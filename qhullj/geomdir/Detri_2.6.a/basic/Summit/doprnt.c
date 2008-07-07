/*
 *  Common code for printf et al.
 *
 *  Copyright 1987, 1988 Steve Summit
 *  Permission to use, modify, and redistribute for non-
 *  commercial purposes is granted so long as this notice
 *  is retained.  Commercial use will require prior approval
 *  from the author.
 *
 *  The calling routine typically takes a variable number of arguments,
 *  and passes the address of the first one.  This implementation
 *  assumes a straightforward, stack implementation, aligned to the
 *  machine's wordsize.  Increasing addresses are assumed to point to
 *  successive arguments (left-to-right), as is the case for a machine
 *  with a downward-growing stack with arguments pushed right-to-left.
 *
 *  To write, for example, fprintf() using this routine, the code
 *
 *	#include <stdio.h>
 *	#include <stdarg.h>
 *
 *	fprintf(fd, format)
 *	FILE *fd;
 *	char *format;
 *	{
 *	va_list argp;
 *	int r;
 *
 *	va_start(argp, format);
 *	r = _doprnt(format, argp, fd);
 *	va_end(argp);
 *	return r;
 *	}
 *
 *  would suffice.
 *
 *  This version implements the following printf features:
 *
 *	%d,%i	decimal
 *	%u	unsigned decimal
 *	%x	hexadecimal
 *	%X	hexadecimal with capital letters
 *	%o	octal
 *	%c	character
 *	%s	string
 *	%e	exponential
 *	%f	floating-point
 *	%g	generalized floating-point
 *	%p	pointer
 *	%m.n	field width, precision
 *	%-	left adjustment
 *	%+	explicit '+' for positive numbers
 *	%space	extra leading space for positive numbers
 *	%0	zero-padding
 *	%l	argument is a long int
 *	%h	(ignored) argument is short
 *	%*.*	width and precision taken from arguments
 *	%#	"alternate format"
 *	%n	intermediate return of # of characters printed
 *
 *  This version implements the following nonstandard features:
 *
 *	%b	binary conversion
 *	%r	roman numeral conversion
 *	%R	roman numeral conversion with capital letters
 *	%v,%V	user-defined conversion (caller supplies formatting function)
 *
 *  If an attempt is made to print a null pointer as a string (0
 *  is passed to %s), "(null)" is printed.  (I think the old
 *  pdp11 doprnt did this.)
 *
 *  The return value is the number of characters printed, or EOF
 *  in case of error.
 *
 *  The %g code is imperfect, as it relies almost entirely upon
 *  gcvt().  In particular, neither %#g, %+g, nor % g work correctly.
 *
 *  %D, %O, and %U are not supported; you should be using %ld, %lo
 *  and %lu if you mean long conversion.
 *
 *  Steve Summit 7/16/88
 */

#include <stdio.h>
#ifdef VARARGS
#include <varargs.h>
#else
#include <stdarg.h>
#endif
#include "printf.h"

#define TRUE 1
#define FALSE 0

/* un#define these to turn off nonstandard features */

#define BINARY
#define ROMAN

#define NULLPTR "(null)"	/* print null pointers thusly */

/*
 *  Un#define FLOATING if you don't need it, want to save space,
 *  or don't have ecvt(), fcvt(), and gcvt().
 */

#define FLOATING

/*
 *  A few other miscellaneous #definitions are possible:
 *
 *	NOUNSLONG	for compilers which don't support unsigned
 *			long ints
 *
 *	NORETURNVALUE	if you don't care about the return value,
 *			and would rather not have each character
 *			printed laboriously counted.
 *
 *	FASTNBF		avoid one-at-a-time write(2)'s, if
 *			possible, when the stdio stream is
 *			unbuffered (see additional comments below)
 *
 *	NOLONG		turn off code for explicit handling of %l,
 *			which works just fine but is a bit
 *			cumbersome and space-consuming.
 *			Don't #define NOLONG.
 *			(Unless you don't use %l at all, NOLONG only
 *			works on machines with sizeof(long)==sizeof(int),
 *			and those tend to be virtual memory machines
 *			for which the extra code size won't be a problem.)
 */

#define NOUNSLONG

#define isdigit(d) ((d) >= '0' && (d) <= '9')
#define Ctod(c) ((c) - '0')

#define MAXBUF 128	/* "very wide fields (>128 characters) fail" */

#ifdef ROMAN
static tack();
static doit();
#endif

#ifdef FLOATING
extern char *ecvt();
extern char *fcvt();
extern char *gcvt();
#endif

_doprnt(fmt, argp, fd)
register char *fmt;
#ifndef ASKF
register
#endif
	 va_list argp;
FILE *fd;
{

#ifdef ASKF

return _do2prnt(0, fmt, &argp, fd);
}

/* only broken up into two routines #ifdef ASKF */

_do2prnt(askf, fmt, argpp, fd)
int askf;
register char *fmt;
va_list *argpp;
FILE *fd;
{
#ifdef LOCALARGP
register va_list *argp = *argpp;
#else
#define argp (*argpp)
#endif

#endif

register char *p;
char *p2;
int size;
int width;
int prec;
char padc;
int n;
unsigned int u;
int base;
char buf[MAXBUF];
int flags;
int negflag;
char signflag;
char *digs;
#ifndef NOLONG
long int l;
#ifdef NOUNSLONG
#define ul l
typedef long int ulong;
#else
unsigned long int ul;
typedef unsigned long int ulong;
#endif
#endif
#ifdef FLOATING
double d;
int decpt;
char echar;
int negexp;
char *p3;
#endif
#ifdef ROMAN
char *rdigs;
int dig;
#endif
#ifndef NORETURNVALUE
int nprinted = 0;
#endif
#ifdef USERDEFINEDPRINTF
int (*func)();
char *ptr;		/* void * if ANSI */
#endif

#ifndef NORETURNVALUE
#define Putc(c, fd) {putc((c), (fd)); nprinted++; }
#else
#define Putc(c, fd) putc((c), (fd))
#endif

while(*fmt != '\0')
	{
	if(*fmt != '%')
		{
#ifdef FASTNBF
		/*
		 *  Two caveats concerning the FASTNBF code:
		 *
		 *	1. Unlike the rest of this code, it peeks
		 *	   inside the fd and expects there to be
		 *	   a field called _flag and a bit therein
		 *	   called _IONBF.
		 *
		 *	2. It doesn't call write() directly, but
		 *	   rather goes through _fwrite(), which
		 *	   centralizes the error checking (setting
		 *	   of _IOERR, etc.) and worries about
		 *	   calling a write function
		 *	   other than write(2) in the case of
		 *	   so-called "functional" stdio's.
		 */

		if(fd->_flag & _IONBF)
			{
			for(p = fmt + 1; *p != '%' && *p != '\0'; p++)
				;
			n = _fwrite(fd, fmt, p - fmt);
#ifndef NORETURNVALUE
			if(n > 0)
				nprinted += n;
#endif
			fmt = p;
			continue;
			}
#endif
		Putc(*fmt++, fd);
		continue;
		}

	fmt++;

	width = 0;
	prec = -1;
	flags = 0;
	signflag = '-';
	padc = ' ';

	while(TRUE)
		{
		if(*fmt == '-')
			{
			flags |= PRF_LADJUST;
			fmt++;
			}
		else if(*fmt == '+' || *fmt == ' ')
			{
			signflag = *fmt;
			fmt++;
			}
		else if(*fmt == '0')
			{
			padc = '0';
#ifdef USERDEFINEDPRINTF
			flags |= PRF_ZEROPAD;
#endif
			fmt++;
			}
		else if(isdigit(*fmt))
			{
			while(isdigit(*fmt))
				width = 10 * width + Ctod(*fmt++);
			}
		else if(*fmt == '*')
			{
			width = va_arg(argp, int);
			fmt++;
			if(width < 0)
				{
				flags ^= PRF_LADJUST;	/* toggle ladjust */
				width = -width;
				}
			}
		else if(*fmt == '.')
			{
			fmt++;
			if(isdigit(*fmt))
				{
				prec = 0;
				while(isdigit(*fmt))
					prec = 10 * prec + Ctod(*fmt++);
				}
			else if(*fmt == '*')
				{
				prec = va_arg(argp, int);
				fmt++;
				}
			}
		else if(*fmt == '#')
			{
			flags |= PRF_NUMSGN;
			fmt++;
			}
		else if(*fmt == 'l')
			{
#ifndef NOLONG
			flags |= PRF_LONG;
#endif
			fmt++;
			}
		else if(*fmt == 'h')
			{
			/*
			 *  Shorts and floats are widened when passed,
			 *  so nothing needs be done here (although
			 *  %hf may be significant for ANSI C).
			 */

			fmt++;
			}
		else	break;
		}

	negflag = FALSE;
	digs = "0123456789abcdef";
#ifdef FLOATING
	echar = 'e';
#endif
#ifdef ROMAN
	rdigs = "  mdclxvi";
#endif

	switch(*fmt)
		{
#ifdef BINARY
		case 'b':
			base = 2;
#ifndef NOLONG
			if(flags & PRF_LONG)
				{
				ul = va_arg(argp, ulong);
				goto dolong;
				}
#endif
			u = va_arg(argp, unsigned);
			goto donum;
#endif
		case 'c':
#ifndef pdp11
			Putc(va_arg(argp, char), fd);
#else
			/* poor little compiler got an "expression */
			/* overflow" on the above */

			n = va_arg(argp, char);
			Putc(n, fd);
#endif
			break;

		case 'd':
		case 'i':
			base = 10;
#ifndef NOLONG
			if(flags & PRF_LONG)
				{
				l = va_arg(argp, long int);

				if(l < 0)
					{
					ul = -l;
					negflag = TRUE;
					}
#ifndef NOUNSLONG
				else	ul = l;
#endif
				goto dolong;
				}
#endif
			n = va_arg(argp, int);

			if(n >= 0)
				u = n;
			else	{
				u = -n;
				negflag = TRUE;
				}

			goto donum;
#ifdef FLOATING
		case 'E':
			echar = 'E';
		case 'e':
			d = va_arg(argp, double);

			if(prec == -1)
				prec = 6;

			p2 = ecvt(d, prec + 1, &decpt, &negflag);

			decpt--;

			if(decpt < 0)
				{
				negexp = TRUE;
				decpt = -decpt;
				}
			else	negexp = FALSE;

			p = &buf[MAXBUF - 1];

			n = 2;

			do	{
				*p-- = digs[decpt % 10];
				decpt /= 10;
				} while(--n > 0 || decpt != 0);

			if(negexp)
				*p-- = '-';
			else	*p-- = '+';

			*p-- = echar;

			if(prec == 0 && (flags & PRF_NUMSGN))
				*p-- = '.';

			for(p3 = p2; *p3 != '\0'; p3++)
				;

			while(p3 > p2)
				{
				*p-- = *--p3;

				if(p3 - p2 == 1)
					*p-- = '.';
				}

			goto putnum;

		case 'f':
			d = va_arg(argp, double);

			if(prec == -1)
				prec = 6;

			p2 = fcvt(d, prec, &decpt, &negflag);

			for(p3 = p2; *p3 != '\0'; p3++)
				;

			p = &buf[MAXBUF - 1];

			if(prec == 0 && (flags & PRF_NUMSGN))
				*p-- = '.';

			while(p3 > p2)
				{
				*p-- = *--p3;

				if(p3 - p2 == decpt)
					*p-- = '.';
				}

			goto putnum;

		case 'G':
			echar = 'E';
		case 'g':
			d = va_arg(argp, double);

			if(prec == -1)
				prec = 6;

			gcvt(d, prec, buf);

			for(p = buf; *p != '\0'; p++)
				;

			for(p2 = p - 1, p = &buf[MAXBUF - 1]; p2 >= buf; p2--)
				{
				if(*p2 == 'e')
					*p-- = echar;
				else	*p-- = *p2;
				}

			goto putpad;
#endif
#ifndef NORETURNVALUE
		case 'n':
			*va_arg(argp, int *) = nprinted;
			break;
#endif
octal:		case 'o':
			base = 8;
#ifndef NOLONG
			if(flags & PRF_LONG)
				{
				ul = va_arg(argp, ulong);
				goto dolong;
				}
#endif
			u = va_arg(argp, unsigned);
			goto donum;

		case 'p':
#ifdef pdp11
			goto octal;
#else
#ifdef M_I86
			ul = va_arg(argp, unsigned long int);

			digs = "0123456789ABCDEF";

			p = &buf[MAXBUF - 1];

			for(n = 0; n < 8; n++)
				{
				if(n == 4)
					*p-- = ':';

				*p-- = digs[ul % 16];
				ul /= 16;
				}

			goto putpad;
#else /* all other machines */
			goto hex;
#endif
#endif
#ifdef ROMAN
		case 'R':
			rdigs = "  MDCLXVI";
		case 'r':
			n = va_arg(argp, int);
			p2 = &buf[MAXBUF - 1];

			dig = n % 10;
			tack(dig, &rdigs[6], &p2);
			n = n / 10;

			dig = n % 10;
			tack(dig, &rdigs[4], &p2);
			n = n / 10;

			dig = n % 10;
			tack(dig, &rdigs[2], &p2);
			n /= 10;

			dig = n % 10;
			tack(dig, rdigs, &p2);

			p = p2;

			goto putpad;
#endif
		case 's':
			p = va_arg(argp, char *);
#ifdef NULLPTR
			if(p == NULL)
				p = NULLPTR;
#endif
			if(width > 0 && !(flags & PRF_LADJUST))
				{
				n = 0;
				p2 = p;

				for(; *p != '\0' &&
						(prec == -1 || n < prec); p++)
					n++;

				p = p2;

				while(n < width)
					{
					Putc(' ', fd);
					n++;
					}
				}

			n = 0;
#ifdef FASTNBF
			if(fd->_flag & _IONBF)
				{
				int r;

				p2 = p;

				while(*p2 != '\0')
					{
					if(n >= prec && prec != -1)
						break;

					p2++;
					n++;
					}

				r = _fwrite(fd, p, n);
#ifndef NORETURNVALUE
				if(r > 0)
					nprinted += r;
#endif
				}
			else
#endif
				while(*p != '\0')
					{
					if(n >= prec && prec != -1)
						break;

					Putc(*p++, fd);
					n++;
					}

			if(n < width && (flags & PRF_LADJUST))
				{
				while(n < width)
					{
					Putc(' ', fd);
					n++;
					}
				}

			break;

		case 'u':
			base = 10;
#ifndef NOLONG
			if(flags & PRF_LONG)
				{
				ul = va_arg(argp, ulong);
				goto dolong;
				}
#endif
			u = va_arg(argp, unsigned);
			goto donum;

		case 'X':
			digs = "0123456789ABCDEF";
hex:		case 'x':
			base = 16;
#ifndef NOLONG
			if(flags & PRF_LONG)
				{
				ul = va_arg(argp, ulong);

dolong:				p = &buf[MAXBUF - 1];

				if(ul == 0)
					flags &= ~PRF_NUMSGN;

				n = prec;

				do	{
#ifdef NOUNSLONG
					/*
					 *  Have to simulate unsigned longs
					 *  with signed ones.
					 */

					/*
					 *  Remember, ul is really l,
					 *  and is signed.
					 */

					if(ul < 0)
						{
						long int ul2;
						int rem;
#define BITSPERCHAR 8
#define HIGHBIT (1L << (sizeof(long int) * BITSPERCHAR - 1))

						ul2 = (ul >> 1) & ~HIGHBIT;

						rem = ul2 % base * 2
								+ (ul & 1);

						*p-- = digs[rem % base];

						ul = ul2 / base * 2
								+ rem / base;
						}
					else
#endif
						{
						*p-- = digs[ul % base];
						ul /= base;
						}
					n--;
					} while(ul != 0 || prec != -1 && n >0);
				goto donumsgn;
				}
#endif
			u = va_arg(argp, unsigned);

donum:			p = &buf[MAXBUF - 1];

			if(u == 0)
				flags &= ~PRF_NUMSGN;

			n = prec;

			do	{
				*p-- = digs[u % base];
				u /= base;
				n--;
				} while(u != 0 || prec != -1 && n > 0);
donumsgn:
			if(flags & PRF_NUMSGN)
				{
				switch(*fmt)
					{
#ifdef BINARY
					case 'b':
						*p-- = 'b';
						*p-- = '0';
						break;
#endif
					case 'x':
						*p-- = 'x';
						/* fall through */
					case 'o':
						*p-- = '0';
						break;

					case 'X':
						*p-- = 'X';
						*p-- = '0';
						break;
					}
				}
putnum:
			if(negflag)
				*p-- = '-';
			else if(signflag != '-')
				*p-- = signflag;
putpad:
			size = &buf[MAXBUF - 1] - p;

			if(size < width && !(flags & PRF_LADJUST))
				{
				while(width > size)
					{
					Putc(padc, fd);
					width--;
					}
				}
#ifdef FASTNBF
			if(fd->_flag & _IONBF)
				{
				n = _fwrite(fd, p + 1, size);
#ifndef NORETURNVALUE
				if(n > 0)
					nprinted += n;
#endif
				}
			else
#endif
				while(++p != &buf[MAXBUF])
					Putc(*p, fd);

			if(size < width)	/* must be ladjust */
				{
				while(width > size)
					{
					Putc(padc, fd);
					width--;
					}
				}

			break;

#ifdef USERDEFINEDPRINTF

		case 'V':
			flags |= PRF_CAPITAL;
		case 'v':
			func = va_arg(argp, int (*)());
			ptr = va_arg(argp, char *);
#ifndef NORETURNVALUE
			nprinted +=
#endif
				    (*func)(fd, ptr, width, prec, flags);
			break;
#endif
		case '\0':
			fmt--;
			break;

		default:
			/* oughta allow caller to register new fmt chars */
			Putc(*fmt, fd);
		}
	fmt++;
	}

#ifdef ASKF
#ifdef LOCALARGP
*argpp = argp;
#endif
#endif

#ifndef NORETURNVALUE

if(ferror(fd))
	return EOF;

return nprinted;

#endif
}

#ifdef ROMAN

static
tack(d, digs, p)
int d;
char *digs;
char **p;
{
if(d == 0) return;
if(d >= 1 && d <= 3)
	{
	doit(d, digs[2], p);
	return;
	}

if(d == 4 || d == 5)
	{
	**p = digs[1];
	(*p)--;
	}

if(d == 4)
	{
	**p = digs[2];
	(*p)--;
	return;
	}

if(d == 5) return;

if(d >= 6 && d <= 8)
	{
	doit(d - 5, digs[2], p);
	**p = digs[1];
	(*p)--;
	return;
	}

/* d == 9 */

**p = digs[0];
(*p)--;
**p = digs[2];
(*p)--;
return;
}

static
doit(d, one, p)
int d;
char one;
char **p;
{
int i;

for(i = 0; i < d; i++)
	{
	**p = one;
	(*p)--;
	}
}

#endif
