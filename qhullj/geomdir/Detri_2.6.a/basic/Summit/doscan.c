/*
 *  Common code for scanf et al.
 *
 *  Copyright 1989 Steve Summit
 *  Permission to use, modify, and redistribute for non-
 *  commercial purposes is granted so long as this notice
 *  is retained.  Commercial use will require prior approval
 *  from the author.
 */

#include <stdio.h>
#include <stdarg.h>
#include <ctype.h>

#undef getc
#define getc fgetc	/* temporary; makes debugging easier */

#define UNGETCKLUDGE	/* for mildly-buggy stdio implementations
			 * in which ungetc after EOF on a "string file"
			 * doesn't work */

#define TRUE 1
#define FALSE 0

#ifndef isxdigit
/* beware: assumes ASCII, and evaluates argument multiple times */
#define isxdigit(c) (isdigit(c) || ((c) >= 'A' && (c) <= 'F') || \
					((c) >= 'a' && (c) <= 'f'))
#endif

/* is scanf's definition of whitespace the same as ctype's? */

#define Iswhite(c) isspace(c)

#define Ctod(c) ((c) - '0')

/* Un#define FLOATING if you don't need it or want to save space. */

#define FLOATING

static int match();

_doscan(fd, fmt, argp)
FILE *fd;
char *fmt;
va_list argp;
{

#ifdef ASKF

return _do2scan(0, fd, fmt, argp);
}

/* only broken up into two routines #ifdef ASKF */

_do2scan(askf, fd, fmt, argp)
int askf;
FILE *fd;
char *fmt;
va_list argp;
{

#endif

register char *fmtp;
register int c;
int retval = 0;
int noassign;
int sizeflag;
int width;
int negflag;
int gotone;
int base;
long int nn;
#ifdef FLOATING
double d;
#endif
char *p;

for(fmtp = fmt; *fmtp != '\0'; fmtp++)
	{
	if(Iswhite(*fmtp))
		{
#ifdef notdef
		while(Iswhite(*fmtp))
			fmtp++;

		/* does this belong here? */
#endif
		do	{
			c = getc(fd);
#ifdef ASKF
			if(askf && c == '\n')
				break;
#endif
			} while(Iswhite(c));
#ifdef ASKF
		if(askf)
			{
			if(c == EOF)
				return 0;
			else if(c == '\n')
				{
				return (*fmtp == '\0') ? retval : -1;
				}
			}
#endif
		ungetc(c, fd);

		continue;
		}

	if(*fmtp != '%')
		{
		c = getc(fd);
		if(c != *fmtp)
			{
			ungetc(c, fd);
			return retval;
			}

		continue;
		}

	/* else it's a conversion specification */

	noassign = FALSE;	/* i.e. do assign */
	sizeflag = '\0';
	width = -1;
	negflag = FALSE;
	gotone = FALSE;

	/* first, collect flags */

	fmtp++;		/* over '%' */

	while(TRUE)
		{
		if(*fmtp == '*')
			{
			noassign = TRUE;
			fmtp++;
			}
		else if(*fmtp == 'h' || *fmtp == 'l')
			{
			sizeflag = *fmtp;
			fmtp++;
			}
		else if(isdigit(*fmtp))
			{
			width = 0;
			while(isdigit(*fmtp))
				width = 10 * width + Ctod(*fmtp++);
			}
		else	break;
		}

	if(*fmtp != '[' && *fmtp != 'c' && *fmtp != '%')	/* % ? */
		{
		/* skip leading whitespace in input */

		do	{
			c = getc(fd);
#ifdef ASKF
			if(askf && c == '\n')
				break;
#endif
			} while(Iswhite(c));
#ifdef ASKF
		if(askf)
			{
			if(c == EOF)
				return 0;
			else if(c == '\n')
				{
				return (*fmtp == '\0') ? retval : -1;
				}
			}
#endif
		ungetc(c, fd);
		}

	switch(*fmtp)
		{
		case 'd':
			base = 10;
donum:
			c = getc(fd);

			if(c == '-')
				{
				negflag = TRUE;
				if(width > 0)
					width--;
				}
			else if(c == '+')
				{
				if(width > 0)
					width--;
				}
			else	ungetc(c, fd);

			/* FALLTHROUGH */

dounsigned:	case 'u':
			/*
			 *  Note that unsigned isn't really treated
			 *  specially, except for not looking for '-'.
			 *  This depends on the essential equivalence
			 *  of simple arithmetic on signed and unsigned
			 *  integers on a normal 2's complement machine.
			 */

			nn = 0;

			while(width < 0 || width-- > 0)
				{
				c = getc(fd);

				if(!((base != 16) ? isdigit(c) : isxdigit(c)))
					{
					ungetc(c, fd);

					if(!gotone)
						return retval;
					else	break;
					}

				if(isdigit(c))
					c -= '0';
				else if(islower(c))
					c -= 'a' - 10;
				else /* isupper(c) */
					c -= 'A' - 10;

				nn = base * nn + c;

				gotone = TRUE;
				}

			if(negflag)
				nn = -nn;

			if(noassign)
				break;

			switch(sizeflag)
				{
				case '\0':
					*va_arg(argp, int *) = (int)nn;
					break;

				case 'h':
					*va_arg(argp, short *) = (short)nn;
					break;

				case 'l':
					*va_arg(argp, long *) = nn;
					break;
				}

			retval++;

			break;

		case 'i':
			c = getc(fd);

			if(c == '0')
				{
				if(width > 0)
					width--;

				c = getc(fd);

				if(c == 'x' || c == 'X')
					{
					if(width > 0)
						width--;
					base = 16;
					}
				else	{
					if(c != EOF)
						ungetc(c, fd);
#ifdef UNGETCKLUDGE
					else	fd->_cnt = 0;
#endif
					ungetc('0', fd); /* in case no others
								following */
					base = 8;
					}
				}
			else	{
				ungetc(c, fd);
				base = 10;
				}

			/* worry about minus sign */

			goto donum;

		case 'o':
			base = 8;
			goto dounsigned;

		case 'x':
		case 'X':
			/* strip leading 0x if present */

			base = 16;

			c = getc(fd);

			if(c == '0')
				{
				if(width > 0)
					width--;
				c = getc(fd);
				if(c == 'x' || c == 'X')
					{
					if(width > 0)
						width--;
					}
				else	{
					if(c != EOF)
						ungetc(c, fd);
#ifdef UNGETCKLUDGE
					else	fd->_cnt = 0;
#endif
					ungetc('0', fd); /* in case no others
								following */
					}
				}
			else	{
				ungetc(c, fd);
				}

			goto dounsigned;

		case 'c':
			if(width < 0)
				width = 1;

			/* FALLTHROUGH */

		case 's':
		case '[':
dostring:		if(!noassign)
				p = va_arg(argp, char *);
			else	p = NULL;

			while(width < 0 || width-- > 0)
				{
				c = getc(fd);

				if(c == EOF)
					break;

				if(*fmtp == 's' && Iswhite(c) ||
					   *fmtp == '[' && !match(c, fmtp + 1))
					{
					ungetc(c, fd);
					break;
					}

				if(p != NULL)
					*p++ = c;

				gotone = TRUE;
				}

			if(!noassign && gotone)
				{
				if(*fmtp != 'c')
					*p = '\0';
				retval++;
				}

			if(*fmtp == '[')
				{
				for(; *fmtp != ']'; fmtp++)
					{
					if(*fmtp == '\0')
						{
						fmtp--;
						break;
						}
					}
				}

			if(c == EOF)
				return retval;

			break;

		case '%':
			c = getc(fd);
			if(c != *fmtp)
				{
				ungetc(c, fd);
				return retval;
				}
			break;
#ifdef FLOATING
		case 'e': case 'E':
		case 'f':
		case 'g': case 'G':

			c = getc(fd);

			if(c == '-')
				{
				negflag = TRUE;
				if(width > 0)
					width--;
				c = getc(fd);
				}
			else if(c == '+')
				{
				if(width > 0)
					width--;
				c = getc(fd);
				}

			d = 0;

			while(isdigit(c) && (width < 0 || width-- > 0))
				{
				d = 10. * d + Ctod(c);
				c = getc(fd);
				}

			if(c == '.')
				{
				double fac = .1;

				if(width > 0)
					width--;

				while(width < 0 || width-- > 0)
					{
					c = getc(fd);

					if(!isdigit(c))
						break;

					d += Ctod(c) * fac;
					fac *= .1;
					}
				}

			if(c == 'e' || c == 'E')
				{
				int negexp = FALSE;
				unsigned int exponent;
				double fac, mag;

				if(width > 0)
					width--;

				c = getc(fd);

				if(c == '-')
					{
					negexp = TRUE;
					if(width > 0)
						width--;
					c = getc(fd);
					}
				else if(c == '+')
					{
					if(width > 0)
						width--;
					c = getc(fd);
					}

				exponent = 0;

				while(isdigit(c) && (width < 0 || width-- > 0))
					{
					exponent = 10 * exponent + Ctod(c);
					c = getc(fd);
					}

				/* compute 10**n by power-of-two powers of 10 */

				mag = 10.;
				fac = 1.;

				while(exponent > 0)
					{
					if(exponent & 1)
						fac *= mag;

					mag *= mag;
					exponent /= 2;
					}

				if(negexp)
					fac = 1. / fac;

				d *= fac;
				}

			ungetc(c, fd);

			if(negflag)
				d = -d;

			if(noassign)
				break;

			switch(sizeflag)
				{
				case '\0':
					*va_arg(argp, float *) = (float)d;
					break;

				case 'l':
					*va_arg(argp, double *) = d;
					break;
				}

			retval++;

			break;
#endif

		/* what's a reasonable default? */
		}
	}

/* Under what conditions should it return EOF? */

return retval;
}

static int
match(c, set)
int c;
char *set;
{
register char *p;
int negflag = FALSE;

if(*set == '^')
	{
	negflag = TRUE;
	set++;
	}

for(p = set; *p != ']' && *p != '\0'; p++)
	{
	if(c == *p)
		return !negflag;
	}

return negflag;
}
