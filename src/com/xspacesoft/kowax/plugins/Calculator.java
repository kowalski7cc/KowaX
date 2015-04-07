package com.xspacesoft.kowax.plugins;

import com.xspacesoft.kowax.kernel.ShellPlugin;
import com.xspacesoft.kowax.kernel.Stdio;
import com.xspacesoft.kowax.shell.CommandRunner;

public class Calculator extends ShellPlugin {
	
	//ArithmeticException
	
	@Override
	public String getAppletName() {
		return "Calc";
	}

	@Override
	public String getAppletVersion() {
		return "1.0A";
	}

	@Override
	public String getAppletAuthor() {
		return "Kowalski";
	}

	@Override
	protected void runApplet(String command, Stdio stdio, CommandRunner commandRunner) {
		try {
			stdio.println(String.valueOf(solve(command, stdio)));
		} catch (ArithmeticException e) {
			if(e.getMessage()==null)
				stdio.println("Error");
			else
				stdio.println("Error: " + e.getMessage());
		}
//		for (int i = 10; i < 100; i++) {
//			stdio.println(i + ": '" + new String(Character.toChars(i)) + "'");
//		}
	}

	private float solve(String expression, Stdio stdio) {
		if (expression.contains("(")) {
			expression = split(expression, stdio);
			return solve(expression, stdio);
		} else {
			if(expression.contains(" "))
				expression = trim(expression);
			float res = 0;
			int start = 0;
			int stop = 0;
			for (int i = 0; i < expression.length(); i++) {
				if(i==0) {
					start=0;
				} else if (i+1 == expression.length()) {
					if(Stdio.isNumber(expression.charAt(i))) {
						String number =  expression.substring(start, expression.length());
//						stdio.println(number);
						if ((Stdio.isNumber(number.charAt(0)))||(number.startsWith("+"))||(number.startsWith("-"))) {
							res = res + Stdio.parseInt(number);
						} else if (number.startsWith("/")) {
							if(Stdio.parseInt(number.substring(1))==0)
								throw new ArithmeticException("Divide by zero");
							else {
								res = res / Stdio.parseInt(number.substring(1));
							}
						} else if (number.startsWith("x")) {
							res = res * Stdio.parseInt(number.substring(1));
						} else {
							throw new ArithmeticException("Invalid operator");
						}
					} else {
						throw new ArithmeticException("Malformed expression");
					}
				} else {
					if(Stdio.isNumber(expression.charAt(i))) {
						
					} else {
						stop = i-1;
						String number =  expression.substring(start, stop+1);
//						stdio.println(number);
						if ((Stdio.isNumber(number.charAt(0)))||(number.startsWith("+"))||(number.startsWith("-"))) {
							res = res + Stdio.parseInt(number);
						} else if (number.startsWith("/")) {
							if(number.substring(1).equals("0"))
								throw new ArithmeticException("Divide by zero");
							else {
								res = res / Stdio.parseInt(number.substring(1));
							}
						} else if (number.startsWith("*")) {
							res = res * Stdio.parseInt(number.substring(1));
						} else {
							throw new ArithmeticException("Invalid operator");
						}
						start = i;
					}
				}
			}
			return res;
		}
	}

	@Override
	public String getDescription() {
		return "A simple calculator for KowaX";
	}

	@Override
	public String getHint() {
		return "Usage: calc (expression)";
	}
	
	private String split(String string, Stdio stdio) {
		int init = 0, fin = 0;
		for (int i = 0; i < string.length(); i++) {
			if(string.charAt(i)==new String(Character.toChars(40)).charAt(0)) {
				init = i;
				break;
			}
		}
		for (int i = string.length(); i >= 0; i--) {
			if(string.charAt(i)==new String(Character.toChars(41)).charAt(0)) {
				fin = i;
				break;
			}
		}
		return string.substring(0, init) + solve(string.substring(init, fin), stdio) + string.substring(fin, string.length());
	}
	
	private String trim(String expression) {
		String buffer = new String(expression);
		for (int i = 0; i < buffer.length(); i++) {
			if(buffer.charAt(i)== ' ') {
				if (i==0)
					buffer = buffer.substring(i+1, buffer.length());
				else if (i+1<buffer.length())
					buffer = buffer.substring(0, i) + buffer.substring(i+1, buffer.length());
				else
					buffer = buffer.substring(0, i);
			}
		}
		return buffer;
	}
}
