/* basic/tokenize.c --- Parse tokens from a string. */

/*--------------------------------------------------------------------------*/

#include "basic.h"

/*--------------------------------------------------------------------------*/

static void shift_left (char string[]);

/*--------------------------------------------------------------------------*/

int basic_tokenize (char string[], char *token[], int maximum)
     /* Input/Output: string[]; NOTE: will be destroyed! */
     /* Output: token[0..maximum-1]. */
     /* This routine parses the given string and breaks it up into
        different tokens.  The tokens are delimited by blanks.
        Use quotes ("balh blah") when a token is to contain blanks.
        Use \" for the quote character and \\ for the backslash.
        The function returns the number of tokens parsed or -1 when it
        would have exceeded the maximum. */
{
  int j = 0, i = 0;
  while (string[i])
    {
      while (string[i] == ' ')
        i ++;
      if (string[i])
        {
          if (j == maximum)
            return (-1);
          token[j++] = &(string[i]);  
          while (string[i] and (string[i] != ' '))
            { /* parse token */
              if ((string[i] == '\\') and (string[i+1] == '"'))
                shift_left (&(string[i]));  /* quote as a character */
              else if (string[i] == '"')
                { /* parse quoted token */
                  shift_left (&(string[i]));
                  while (string[i])
                    {
                      if ((string[i] == '\\') and (string[i+1] == '"'))
                        { /* quote as a character within quoted string */
                          shift_left (&(string[i]));
                          i ++;
                        }
                      else if (string[i] == '"')
                        string[i] = 0;
                      else
                        i ++;
                    }
                }
              i ++;
            }
          if (string[i])
            {
              string[i] = 0;
              i ++;
            }
        }
    }
  return (j);
}

/*--------------------------------------------------------------------------*/

static void shift_left (char string[])
     /* Input/Output: string[]. */
     /* Shifts non-empty string[0..] to string[1..] */
{
  int i = 0;
  while (string[i])
    {
      string[i] = string[i+1];
      i ++;
    }
}
