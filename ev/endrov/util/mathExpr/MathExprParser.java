package endrov.util.mathExpr;

import java.util.HashSet;
import java.util.LinkedList;

/**
 * Parser for mathematical expressions
 * 
 * @author Johan Henriksson
 *
 */
public class MathExprParser
	{
	//All operators accepted, in different priority levels
	private static final HashSet<String> opa4=new HashSet<String>();
	private static final HashSet<String> opa3=new HashSet<String>();
	private static final HashSet<String> opa2=new HashSet<String>();
	private static final HashSet<String> opa1=new HashSet<String>();
	static
		{
		opa1.add("&");
		opa1.add("&&");
		opa1.add("|");
		opa1.add("||");
	
		opa2.add("<");
		opa2.add(">");
		opa2.add("<=");
		opa2.add(">=");
		opa2.add("=<");
		opa2.add("=>");
		opa2.add("==");
		
		opa3.add("+");
		opa3.add("-");

		opa4.add("*");
		opa4.add("/");
		}

	/**
	 * Exception raised on parse error
	 */
	public static class ParseException extends Exception
		{
		private static final long serialVersionUID = 1L;
		public ParseException(String message)
			{
			super(message);
			}
		}
	
	
	
	private static boolean isSymbol(String s)
		{
		return Character.isLetter(s.charAt(0));
		}

	/**
	 * Tokenizer for expressions
	 */
	private static class Tokenizer
		{
		public LinkedList<String> tokens=new LinkedList<String>(); //May want to encapsulate in types
		private static String opchar="+-*/&|%=#!<>";
		
		public Tokenizer(String s) throws ParseException
			{
			int ac=0;
			while(ac<s.length())
				{
				char c=s.charAt(ac);
				if(c=='(')
					{
					tokens.add("(");
					ac++;
					}
				else if(c==')')
					{
					tokens.add(")");
					ac++;
					}
				else if(c==',')
					{
					tokens.add(",");
					ac++;
					}
				else if(Character.isDigit(c))
					{
					//Read a number
					StringBuilder sb=new StringBuilder();
					for(;;)
						{
						if(ac==s.length())
							break;
						c=s.charAt(ac);
						//Read up to the "." or end of number
						if(Character.isDigit(c))
							{
							sb.append(c);
							ac++;
							}
						else if(c=='.')
							{
							sb.append(c);
							ac++;
							break;
							}
						else
							break;
						}
					//Read any decimals
					for(;;)
						{
						if(ac==s.length())
							break;
						c=s.charAt(ac);
						if(Character.isDigit(c))
							{
							sb.append(c);
							ac++;
							}
						else
							break;
						}
					tokens.add(sb.toString());
					}
				else if(Character.isWhitespace(c))
					{
					//Just skip
					ac++;
					}
				else if(Character.isLetter(c))
					{
					//Read a token
					StringBuilder sb=new StringBuilder();
					for(;;)
						{
						if(ac==s.length())
							break;
						c=s.charAt(ac);
						if(Character.isLetter(c) || Character.isDigit(c))
							{
							sb.append(c);
							ac++;
							}
						else
							break;
						}
					tokens.add(sb.toString());
					}
				else if(opchar.contains(""+c))
					{
					//Read an operation
					StringBuilder sb=new StringBuilder();
					for(;;)
						{
						if(ac==s.length())
							break;
						c=s.charAt(ac);
						if(opchar.contains(""+c))
							{
							sb.append(c);
							ac++;
							}
						else
							break;
						}
					tokens.add(sb.toString());
					}
				else
					throw new ParseException("Unable to parse char "+c);
				}
			}
		
		
		/**
		 * Check if the next token is as given. If so, accept it. Returns true if accepted
		 */
		public boolean nextIs(String s)
			{
			if(tokens.isEmpty())
				return false;
			else
				{
				if(tokens.getFirst().equals(s))
					{
					accept();
					return true;
					}
				else
					return false;
				}
			}
		

		/**
		 * Require that the next token is as given
		 */
		public void expect(String s) throws ParseException
			{
			if(tokens.isEmpty())
				throw new ParseException("Expected "+s+", but at end of input");
			else
				{
				if(tokens.getFirst().equals(s))
					tokens.removeFirst();
				else
					throw new ParseException("Expected "+s+", got "+tokens.getFirst());
				}
			}


		/**
		 * Look ahead one character
		 */
		public String next()
			{
			if(tokens.isEmpty())
				return null;
			else
				return tokens.getFirst();
			}

		/**
		 * Accept the next character (remove it)
		 */
		public void accept()
			{
			tokens.removeFirst();
			}
		}
	
	
	
	
	
	
	

	/**
	 * Parse an expression from the string. Returns null if the string is only whitespace.
	 * Throws exception on parse error
	 */
	public static MathExpr parse(String s) throws ParseException
		{
		Tokenizer tok=new Tokenizer(s);
		System.out.println(tok.tokens);
		if(tok.tokens.isEmpty())
			return null;
		else
			return parse1(tok);
		}
	
	
	
	

	private static MathExpr parse1(Tokenizer tok) throws ParseException
		{
		MathExpr a=parse2(tok);
		
		String n=tok.next();
		if(n!=null && opa1.contains(n))
			{
			tok.accept();
			MathExpr b=parse1(tok);
			return new MathExprFunction(n, a, b);
			}
		else
			return a;
		}
	
	private static MathExpr parse2(Tokenizer tok) throws ParseException
		{
		MathExpr a=parse3(tok);
		
		String n=tok.next();
		if(n!=null && opa2.contains(n))
			{
			tok.accept();
			MathExpr b=parse2(tok);
			return new MathExprFunction(n, a, b);
			}
		else
			return a;
		}

	private static MathExpr parse3(Tokenizer tok) throws ParseException
		{
		MathExpr a=parse4(tok);
		
		String n=tok.next();
		if(n!=null && opa3.contains(n))
			{
			tok.accept();
			MathExpr b=parse3(tok);
			return new MathExprFunction(n, a, b);
			}
		else
			return a;
		}
	
	private static MathExpr parse4(Tokenizer tok) throws ParseException
		{
		if(tok.nextIs("("))
			{
			MathExpr e=parse1(tok);
			tok.expect(")");
			return e;
			}
		else if(tok.nextIs("!"))
			{
			MathExpr e=parse1(tok);
			return new MathExprFunction("!",e);
			}
		else
			{
			String symOrNum=tok.next();
			
			MathExpr a;
			if(isSymbol(symOrNum))
				a=new MathExprSymbol(tok.next());
			else
				a=new MathExprNumber(tok.next());
			tok.accept();
			
			String n=tok.next();
			if(n!=null && opa4.contains(n))
				{
				tok.accept();
				MathExpr b=parse4(tok);
				return new MathExprFunction(n, a, b);
				}
			else if(n!=null && n.equals("(") && a instanceof MathExprSymbol)
				{
				//Function
				tok.accept();
				String functionName=symOrNum;
				LinkedList<MathExpr> args=new LinkedList<MathExpr>();

				n=tok.next();
				if(n==null)
					throw new ParseException("Function does not end with )");
				if(n.equals(")"))
					{
					tok.accept();
					}
				else
					{
					args.add(parse1(tok));
					
					for(;;)
						{
						n=tok.next();
						if(n==null)
							throw new ParseException("Function does not end with )");
						if(n.equals(")"))
							{
							tok.accept();
							break;
							}
						else
							{
							tok.expect(",");
							args.add(parse1(tok));
							}
						}
					}
				
				return new MathExprFunction(functionName, args.toArray(new MathExpr[]{}));
				}
			else
				return a;
			}
		}

	
	
	public static void main(String[] args)
		{
		try
			{
			System.out.println(parse("c*(a+b)+f(1,2.0)"));
			}
		catch (ParseException e)
			{
			e.printStackTrace();
			}
		}

	}
