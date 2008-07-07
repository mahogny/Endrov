/* basic/getarg.c */


/* The getarg routines were posted in alt.sources; article written Jul 11,
   1990 by darcy@druid.uucp.  I manipulated the source slightly, mainly to
   understand it and include it into my Basic C Library.

   MODIFICATIONS and REMARKS:
   - had trouble (segmentation fault, what else) with initarge(); don't use it!
   - changed "optind" and "optarg" to "basic_getarg_optind" and
     "basic_getarg_optarg" to and added the suffix "basic_" to all
     other external identifiers.
   - NOTE: basic_getarg_init() doesn't really initialize, it rather "stuffs"
     or adds the new argument list to the existing one, when the latter isn't
     totally parsed yet.  (This is a feature!  See D'Arcy's description in
     getarg.doc.)  Ergo: Always parse ALL arguments before, eg, aborting
     because of error!
   --EPM <mucke@cs.uiuc.edu> */


/* getarg.c
   originally written by D'Arcy J.M. Cain
   D'Arcy Cain Consulting
   275 Manse Road, Unit # 24
   West Hill, Ontario
   M1E 4X8
   416 281 6094
   UUCP: darcy@druid */


#if 0   
/* Originally, the text of the DESCRIPTION was in the source file,
   embraced by #if 0  ...  #endif. Now, it's in the README.getarg file. */
#endif

#if defined(sun)
# include <string.h>
#elif defined(__STRICT_ANSI__) && defined(__USE_FIXED_PROTOTYPES__)
# include <string.h> 
  extern char *strchr (const char *, int);  /* missing in <string.h> ?? */
#elif defined(BSD) || defined(__GNUC__)
# include <strings.h>
#else
# include <string.h>
#endif

#ifndef  index
# define index strchr
#endif
#ifndef  rindex
# define rindex strrchr
#endif /* added rindex for consistency and changed all str{r}chr below --EPM */

#include <stdlib.h>
#if !(defined (__GNUC__) || (__convex__))
#include <malloc.h>
#endif
#include <ctype.h>

#ifndef NULL
#define NULL 0
#endif

int  basic_getarg_optind = 0;
char *basic_getarg_optarg;

static char **pargv = NULL;
static int pargc = 0;

int basic_getarg_init (int argc, char **argv)
{
  int k = argc * (int) sizeof (char *);

  /* check for trivial case */
  if (!argc)
    return (0);

  /* get or expand space */
  if (pargc == 0)
    pargv = (char **) malloc ((unsigned) k);
  else
    pargv = (char **) realloc ((char *) pargv, (unsigned) pargc + k);
  
  if (pargv == NULL)
    return (-1); /* not enough memory for argument pointers */
  
  /* if adding arguments insert them at current argument */
  if (pargc)
    for (k = pargc - 1; k >= basic_getarg_optind; k--)
      pargv[k + argc] = pargv[k];
  
  for (k = 0; k < argc; k++)
    pargv[basic_getarg_optind + k] = argv[k];
  
  pargc += argc;
  return (pargc);
}


int basic_getarg_inite (int argc, char **argv)
{
  char *env_str, *env_args[64];
  int  k, j = 0;

#ifdef __MSDOS__
  char prog_name[64];
#endif
  
  if ((k = basic_getarg_init(argc - 1, argv + 1)) == -1)
    return (-1);  /* not enough memory for argument pointers */
  
#ifdef  __MSDOS__

  if ((env_str = rindex (argv[0], '\\')) == NULL)
    {
      (void) strcpy (prog_name, argv[0]);
      if ((env_str = index (prog_name, ':')) != NULL)
        (void) strcpy (prog_name, env_str + 1);
    }
  else
    (void) strcpy (prog_name, env_str + 1);
  
  if ((env_str = index (prog_name, '.')) != NULL)
    *env_str = 0;
  
  if ((env_str = getenv (prog_name)) == NULL)

#else

    if ((env_str = (char *) rindex (argv[0], '/')) != NULL)
      env_str++;
    else
      env_str = argv[0];
  
  if ((env_str = getenv (env_str)) == NULL)

#endif

    return(k);  /* Note that this is the body of the "if" above!  --EPM */
  
  if ((env_args[0] = (char*) malloc (strlen (env_str) + 1)) == NULL)
    return(-1);  /* not enough memory for argument pointers */
  
  env_str = strcpy (env_args[0], env_str);
  
  while (isspace (*env_str))
    env_str++;
  
  while (*env_str)
    {
      if (*env_str == '"')
        {
          env_args[j++] = ++env_str;
          
          while (*env_str && *env_str != '"')
            {
              if (*env_str == '\\')
                {
                  (void) strcpy (env_str, env_str + 1);
                  env_str++;
                }
              env_str++;
            }
        }
      else
        {
          env_args[j++] = env_str;
          
          while (*env_str && !isspace (*env_str))
            env_str++;
        }
      
      if (*env_str)
        *env_str++ = 0;
      
      while (*env_str && isspace (*env_str))
        env_str++;
    }
  
  if ((j = basic_getarg_init (k, env_args)) == 0)
    return (-1);  /* not enough memory for argument pointers */
  
  return(j + k);
}


/* The meat of the module.  This returns options and arguments similar to
   getopt() as described above. */


int basic_getarg (const char *opts)
{
  static int sp = 0, end_of_options = 0;
  int c;
  char *cp;
  
  basic_getarg_optarg = NULL;
  
  /* return 0 if we have read all the arguments */
  if (basic_getarg_optind >= pargc)
    {
      if (pargv != NULL)
        free ((char *) pargv);
      pargv = NULL;
      pargc = 0;
      basic_getarg_optind = 0;
      return(0);
    }
  
  /* Are we starting to look at a new argument? */
  if (sp == 0)
    {
      /* return it if it is a file name */
      if ((*pargv[basic_getarg_optind] != '-') || end_of_options)
        {
          basic_getarg_optarg = pargv[basic_getarg_optind++];
          return (-1);
        }
      
      /* special return for standard input */
      if (strcmp (pargv[basic_getarg_optind], "-") == 0)
        {
          basic_getarg_optind++;
          return ('-');
        }
      
      /* "--" signals end of options */
      if (strcmp(pargv[basic_getarg_optind], "--") == 0)
        {
          end_of_options = 1;
          basic_getarg_optind++;
          return (basic_getarg (opts));
        }
      
      /* otherwise point to option letter */
      sp = 1;
    }
  else if (pargv[basic_getarg_optind][++sp] == 0)
    {
      /* recursive call if end of this argument */
      sp = 0;
      basic_getarg_optind++;
      return (basic_getarg (opts));
    }
  
  c = pargv[basic_getarg_optind][sp];
  
  if (c == ':' || (cp = (char *) index (opts, c)) == NULL)
    return ('?');
  
  if (*++cp == ':')
    {
      /* Note the following code does not allow leading
         spaces or all spaces in an argument */
      
      while (isspace (pargv[basic_getarg_optind][++sp]))
        ;
      
      if(pargv[basic_getarg_optind][sp])
        basic_getarg_optarg = pargv[basic_getarg_optind++] + sp;
      else if (++basic_getarg_optind >= pargc)
        c = '?';
      else
        basic_getarg_optarg = pargv[basic_getarg_optind++];
      
      sp = 0;
    }
  else if (*cp == ';')
    {
      while (isspace (pargv[basic_getarg_optind][++sp]))
        ;
      
      if (pargv[basic_getarg_optind][sp])
        basic_getarg_optarg = pargv[basic_getarg_optind] + sp;
      
      basic_getarg_optind++;
      sp = 0;
    }
  
  return(c);
}
