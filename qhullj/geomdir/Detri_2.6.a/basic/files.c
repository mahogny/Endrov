/* basic/files.c --- Encapsulated file utilities.  Cf, man fopen. */

/*--------------------------------------------------------------------------*/

#define AUTOMATIC_UNCOMPRESS  /* Uncomment line to turn feature off. */

/* If AUTOMATIC_UNCOMPRESS is on, than basic_fopen (pathname, "r") will first
   look for a compressed file, uncompress it (via a Unix pipe) and read it
   (from the pipe).  Is the mode == "w", basic_fopen() will first delete the
   compressed file, and then create a new file pathname.

   The first call of basic_fopen() will decide on what compression scheme is
   used. */

/*--------------------------------------------------------------------------*/

#include "basic.h"
#include <unistd.h>
#include <sys/stat.h>

/* Below declarations should come form <stdio.h>, but they don't always;
   e.g., for 'cc -ansi' on the SGIs. */
#if defined (is_ANSI_C)
  FILE *popen (const char *command, const char *type);
  int pclose (FILE *file);
# if !defined (fileno)
    int fileno (FILE *stream);
#endif
#endif

/*--------------------------------------------------------------------------*/

#ifdef AUTOMATIC_UNCOMPRESS

typedef struct plist_record
{
  int id;
  struct plist_record *next;
} Plist;
static Plist *plist = NULL;  /* local linked list remembers files that were
                                open with popen(); NOTE: no de-allocation! */

static const char *frmt[] = { "", "%s.Z",         "%s.z",      "%s.gz",    0 };
static const char  *cmd[] = { "", "compress -dc", "gunzip -c", "gzip -dc", 0 };
static int scheme = 0;

static void select_scheme (int *scheme, const char fname[]);
     /* select from supported compression schemes */

#endif

char *basic_fopen_zpath = NULL;  /* exports actual file path of last
                                    basic_fopen() call, *if* file was
                                    compressed & user wants to know */

/*--------------------------------------------------------------------------*/

FILE* basic_fopen (const char path_name[], const char mode[])
     /* Opens file with given path_name in given mode. Cf, man fopen. */
{
  FILE *file;
  char fname[MAXPATHLEN];
  char fmode[20];
  sprint (fname, "%s", path_name);
  sprint (fmode, "%s", mode);
#ifndef AUTOMATIC_UNCOMPRESS
  {
    file = fopen (fname, fmode);
    if (not file)
      basic_error ("basic_fopen: Can't open file \"%s\" with mode \"%s\".", 
                   fname, fmode);
  }
#else
  { /* check for compressed file first */
    static char zname[MAXPATHLEN];
    basic_fopen_zpath = NULL;
    if (scheme == 0)
      select_scheme (&scheme, fname);
    sprint (zname, frmt[scheme], fname);
    if (    (scheme != 0)  /* in this case, zname == "" */
        and (access (zname, F_OK) != -1))
      {
        if (strpbrk (fmode, "wW"))
          {
            if (access (zname, W_OK) == -1)
              basic_error ("basic_fopen: Can't remove file \"%s\".",
                           zname);
            print ("Removing compressed file \"%s\" ...\n", zname);
            basic_system ("rm -f %s", zname);
            file = fopen (fname, fmode);
          }
        else
          { /* open compressed file for reading using popen() */
            char buffer[20+MAXPATHLEN];
            sprintf (buffer, "%s %s", cmd[scheme], zname);
            file = popen (buffer, "r");
            if (file)
              { /* push file id onto plist, use malloc() to hide it */
                Plist *new = (Plist *) malloc (sizeof (Plist));
                new->id = fileno (file);
                new->next = plist;
                plist = new;
              }
            basic_fopen_zpath = zname;  /* export compressed file name,
                                           in case caller wants to know */
          }
      }
    else
      file = fopen (fname, fmode);
    if (not file)
      basic_error ("basic_fopen: Can't open file \"%s\", %s \"%s\".",
                   fname, "with mode", fmode);
  }
#endif
  return (file);
}

/*--------------------------------------------------------------------------*/

void basic_fclose (FILE *file)
     /* Closes the given file.  Cf, man fclose. */
{
#ifndef AUTOMATIC_UNCOMPRESS
  if (fclose (file) != 0)
    fprint (stderr, "WARNING: basic_fclose: Unsucessful\n");
#else
  Plist *p = plist;
  while (p != NULL)
    { /* if file is in plist, using pclose() */
      if (p->id == fileno (file))
        { 
          if (pclose (file) != 0)
            fprint (stderr, "WARNING: basic_fclose: Unsucessful pclose()\n");
          p->id = 0;  /* forget about the file id */
          return;
        }
      p = p->next;
    }
  /* otherwise, use fclose() */
  if (fclose (file) != 0)
    fprint (stderr, "WARNING: basic_fclose: Unsucessful fclose()\n");
#endif
}

/*--------------------------------------------------------------------------*/

#ifdef AUTOMATIC_UNCOMPRESS

static void select_scheme (int *scheme, const char fname[])
     /* Output: scheme */
{
  char zname[MAXPATHLEN];
  int i = 1;
  while (frmt[i])
    {
      sprint (zname, frmt[i], fname);
      if (access (zname, F_OK) != -1)
        {
          *scheme = i;
#if 1
          print ("(Using \"%s %s\" for decompression.)\n",
                 cmd[i], frmt[i]);
#endif    
          break;
        }
      i ++;
    }
}

#endif

/*--------------------------------------------------------------------------*/

int basic_access (const char path_name[])
     /* Checks if file with given path_name exists. Cf, man access. */
{
#ifndef AUTOMATIC_UNCOMPRESS
  return (access (path_name, F_OK) != -1);
#else
  if (access (path_name, F_OK) != -1)
    return (TRUE);
  else
    { /* check for path_name.Z */
      char zname[MAXPATHLEN];
      if (scheme == 0)
        select_scheme (&scheme, path_name);
      sprint (zname, frmt[scheme], path_name);
      return (access (zname, F_OK) != -1); 
    }
#endif
}

/*--------------------------------------------------------------------------*/

time_t basic_modtime (const char path_name[])
     /* Returns last modifaction time of file, measured in (time_t) seconds
        since 00:00:00, Jan 1, 1970.  NOTE: Figuring out whether the file
        is compressed or not is a hack! */
{
  time_t no_access = (time_t) 0;
  if (not basic_access (path_name))
    return (no_access);
  else
    {
      struct stat buf;
      if (stat (path_name, &buf) == 0)
        return (buf.st_mtime);
      else
        { /* check for compressed file; spaghetti code... */
          char zname[MAXPATHLEN];
          sprint (zname, frmt[scheme], path_name);
          if (stat (zname, &buf) == 0)
            return (buf.st_mtime);
          else
            return (no_access);
        }
    }
}

/*--------------------------------------------------------------------------*/

void basic_rm (const char path_name[])
     /* Removes file with given path_name[]. */
{
  /* Note: shouldn't we test for existence or whether it's a directory? */
  if (unlink (path_name) != 0)
    fprint (stderr, "WARNIG: basic_rm (\"%s\") failed!\n", path_name);
}
