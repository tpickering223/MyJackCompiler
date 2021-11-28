import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;

public class JackTokenizer {
	File input = null;
	String currentToken = null;
	String literal = null;
	String nextToken = null;
	FileWriter writeOut;
	Token tokenType;
	KeyWord keyword;
	Scanner stream;
	Scanner subtokens;
	boolean inComment;
	boolean inLiteral;
	boolean semicolon;
	boolean hasSubTokens;
	boolean classfield;
	boolean functionDec;
	boolean isCall;
	boolean isArray;
	
	ArrayList<String> symbols = new ArrayList<String>(Arrays.asList("{", "}", "(", ")", "[", "]", ".", 
			",", ";",  "+",  "-", "*", "/", "&", 
			"|", "<", ">", "=", "~"));
	ArrayList<String> buffer = new ArrayList<String>();
	
	
	
	public JackTokenizer(File source) throws IOException {
		input = source;
		try {
			stream = new Scanner(source);
			writeOut = new FileWriter(new File(source.getAbsolutePath().replace(".jack", "TP.xml")));
			System.out.println("Token file generated at path: " + source.getName().replace(".jack", "TP.xml"));
		} catch (FileNotFoundException e) {
			System.out.println("Source File not found!");
			e.printStackTrace();
		}
		inComment = false;
		inLiteral = false;
		hasSubTokens = false;
		classfield = false;
		functionDec = false;
		isCall = false;
		isArray = false;
		writeOut.write("<tokens>\n");
	}
	
	public boolean hasMoreTokens() {
		return stream.hasNext();
	}
	
	public void advance() throws IOException {	
		semicolon = false;
		boolean gotToken = false;
		if(hasSubTokens) {
			subtokens = new Scanner(nextToken);
		    
		}
		while(!gotToken) {
			if(bufferhasNext()) {
				currentToken = next();
				buffer.remove(0);
			}
			
			else if(!hasMoreTokens() && buffer.isEmpty()) {
				writeOut.write("</tokens>\n");
				return;
			}
			else {
				currentToken = stream.next();
			}
			while(currentToken.startsWith("//") || currentToken.equals("//")) {
				stream.nextLine();
				currentToken = stream.next();
			}
			if(currentToken.startsWith("/*")) {
				inComment = true;
				while(inComment) {
					currentToken = stream.next();
					if(currentToken.contains("*/") || currentToken.endsWith("*/")) {
						inComment = false;
						currentToken = stream.next();
					}
				}
			}
			else if(currentToken.equals("") || currentToken.equals(" ")) {
				stream.nextLine();
				currentToken = stream.next();
			}
			
			/*if(currentToken.length() > 1) {
				if(currentToken.contains("(") || currentToken.contains(")") || currentToken.contains("[") || currentToken.contains("]") || currentToken.contains(",") || currentToken.contains(";")) {
					currentToken = divideToken(currentToken);
				}
			}
			*/
			if(!currentToken.startsWith("\""))
				currentToken = divideToken(currentToken);
			else {
				while(getCharCount('\"') < 2) {
					currentToken += stream.next(); 
				}
				separateLiteral();
			}
			
			/*if(currentToken.endsWith(";")) {
				currentToken.replace(";", "");
				semicolon = true;
			}
			*/
			if(isKeyWord(currentToken)) {
				tokenType = Token.KEYWORD;
				gotToken = true;
				if(currentToken.equals("static") || currentToken.equals("var") || currentToken.equals("field")) {
					classfield = true;
				}
				else if(currentToken.equals("function") || currentToken.equals("method") || currentToken.equals("constructor") ) {
					classfield = false;
				}
			}
			else if(symbols.contains(currentToken)) {
				tokenType = Token.SYMBOL;
				gotToken = true;
				
			}
			else if(currentToken.startsWith("\"") && (currentToken.endsWith("\";") || currentToken.endsWith("\""))) {
				
				semicolon = true;
				tokenType = Token.STRING_CONST;
				literal = string_literal();
				currentToken = literal;
				gotToken = true;
			}
			else {
				try {
					Integer.parseInt(currentToken);
					tokenType = Token.INT_CONST;
					gotToken = true;
					
				}
				catch (Exception e) {
					// Do nothing.
				}
				if(!gotToken) {
					tokenType = Token.IDENTIFIER;
					gotToken = true;
				}
			}
			
		}
		writeToken(currentToken);
		
	}
	
	public void setCallValue(boolean val) {
		isCall = val;
	}
	public void writeToken(String input) throws IOException {
		if(tokenType() == Token.KEYWORD) {
			writeOut.write("<keyword> ");
			writeOut.write(input);
			writeOut.write(" </keyword>\n");
		}
		else if(tokenType() == Token.IDENTIFIER) {
			writeOut.write("<identifier> ");
			writeOut.write(input);
			writeOut.write(" </identifier>\n");
		}
		else if(tokenType() == Token.SYMBOL) {
			writeSymbol(input);
		}
		else if(tokenType() == Token.INT_CONST) {
			writeOut.write("<integerConstant> ");
			writeOut.write(input);
			writeOut.write(" </integerConstant>\n");
		}
		else if(tokenType() == Token.STRING_CONST) {
			writeOut.write("<stringConstant> ");
			writeOut.write(input);
			writeOut.write(" </stringConstant>\n");
		}
	}
	
	public String getNext() {
	  if(!buffer.isEmpty()) {	
		return buffer.get(0);
	  }
	  else {
		  return "";
	  }
	}
	
	public String separateLiteral() {
		String first, second;
		if(currentToken.length() == 1) {
			return currentToken;
		}
		for(int i = currentToken.lastIndexOf("\""); i <= currentToken.length() - 1; i++) {
			while(Character.toString(currentToken.charAt(i)).equals(")")) {
				
				currentToken = currentToken.replace(")", "");
				buffer.add(")");
				
			}
			while(Character.toString(currentToken.charAt(i)).equals(";")) {
				
				currentToken = currentToken.replace(";", "");
				buffer.add(";");
				return currentToken;
			}
			
		}
		return currentToken;
	}
	
	public int getCharCount(char in) {
		int count = 0;
		for(int i = 0; i < currentToken.length(); i++) {
			if(currentToken.charAt(i) == in) {
				count++;
			}
		}
		return count;
	}
	
	public boolean isKC() {
		if(currentToken.equals("true") || currentToken.equals("false") || currentToken.equals("null") || currentToken.equals("this")) {
			return true;
		}
		else {
			return false;
		}
	}
	
	public String divideToken(String token) {
		/*String[] parts;
		if(token.contains(".")) {
			parts = token.split(".");
			buffer.add(".");
			for(int i = 1; i < parts.length; i++) {
				buffer.add(parts[i]);
			}
			return parts[0];
		}
		else if(!token.startsWith("(") && token.contains("(")) {
			
		}
		else if(token.endsWith("()") && token.length() > 2) {
			token.replace("()", "");
			buffer.add("(");
			buffer.add(")");
			return token;
		}
		
		else if(token.endsWith(")")) {
			token.replace(")", "");
			buffer.add(")");
		}
		*/
		String first, second;
		if(token.length() == 1) {
			return token;
		}
		for(int i = 0; i < token.length(); i++) {
			if(symbols.contains(Character.toString(token.charAt(i)))) {
				if(token.charAt(i) == '.') {
					isCall = true;
				}
				else if(token.charAt(i) == '[') {
					isArray = true;
				}
				else if (token.charAt(i) == ']') {
					isArray = false;
				}
				else if(token.charAt(i) == ';') {
					isCall = false;
					isArray = false;
				}
				if(i==0) {
					first = token.substring(0, i+1);
				}
				else {
					first = token.substring(0, i);
					buffer.add(Character.toString(token.charAt(i)));
				}
				if(i == token.length() -  1) {
					second = token.substring(token.length() -1, token.length());
				}
				else {
					second = token.substring(i + 1, token.length());
					buffer.add(second);
				}
				
				token = first;
				return token;
			}
			
		}
		return token;
	}
	
	public boolean isArray() {
		return isArray;
	}
	
	public boolean isCall() {
		return isCall;
	}
	
	public boolean containsSemicolon() {
		return semicolon;
	}
	
	public String removeComments(String line) {
		if(line.contains("//")) {
			line = line.substring(0, line.indexOf('/'));
			return line;
		}
		else {
			return line;
		}
	}
	
	public boolean isStaticfield() {
		return classfield;
	}
	
	public Token tokenType() {
		return tokenType;
	}
	
	public KeyWord getKeyWord() {
		return keyword;
	}
	
	public char symbol() {
		char temp = currentToken.charAt(0);
		return temp;
	}
	
	public String identifier() {
		return currentToken;
	}
	
	public int numValue() {
		return Integer.parseInt(currentToken);
	}
	
	public String string_literal() {
		String temp = currentToken.replaceAll("\"", "");
		temp = temp.replaceAll(";", "");
		
		return temp;
	}
	
	public boolean bufferhasNext() {
		return !buffer.isEmpty();
	}
	
	public String next() {
		return buffer.get(0);
	}
	
	public String getAtIndex(int num) {
		return buffer.get(num);
	}
	
	public boolean inFunction() {
		return functionDec;
	}
	
	public boolean isOp() {
		if(currentToken.equals("+") || currentToken.equals("-") || currentToken.equals("/") || currentToken.equals("&") || currentToken.equals("|")  || currentToken.equals("<") || currentToken.equals(">") || currentToken.equals("=") || currentToken.equals("*")) {
			return true;
		}
		return false;
	}
	
	public boolean isUOP() {
		if(currentToken.equals("-") || currentToken.equals("~")) {
			return true;
		}
		return false;
	}
	
	public boolean isKeyConst() {
		if(currentToken.equals("true") || currentToken.equals("false") || currentToken.equals("null") || currentToken.equals("this")) {
			return true;
		}
		return false;
	}
	
	public void writeSymbol(String symbol) throws IOException {
		writeOut.write("<symbol> ");
		if(symbol.equals("<")) {
			 writeOut.write("&lt;");
			 
		 }
		else if(symbol.equals("&")) {
			writeOut.write("&amp;");
		}
		else if(symbol.equals(">")) {
			writeOut.write("&gt;");
		}
		else if(symbol.equals("\"")) {
			writeOut.write("&quot;");
		}
		else {
			writeOut.write(symbol);
		}
		writeOut.write(" </symbol>\n");
	}
	
	public boolean isKeyWord(String input) {
		
		switch((input.toUpperCase())) {
				case "BOOLEAN":
					keyword = KeyWord.BOOLEAN;
					return true;
					
				case "CHAR":
					keyword = KeyWord.CHAR;
					return true;
				
				case "CLASS":
					keyword = KeyWord.CLASS;
					return true;
				
				case "CONSTRUCTOR":
					keyword = KeyWord.CONSTRUCTOR;
					return true;
				
				case "DO":
					keyword = KeyWord.DO;
					return true;
				
				case "ELSE":
					keyword = KeyWord.ELSE;
					return true;
				
				case "FALSE":
					keyword = KeyWord.FALSE;
					return true;
				
				case "FIELD":
					keyword = KeyWord.FIELD;
					return true;
				
				case "FUNCTION":
					keyword = KeyWord.FUNCTION;
					return true;
			
				case "IF":
					keyword = KeyWord.IF;
					return true;
				
				case "INT":
					keyword = KeyWord.INT;
					return true;
				
				case "LET":
					keyword = KeyWord.LET;
					return true;
			
				case "METHOD":
					keyword = KeyWord.METHOD;
					return true;
		
				case "NULL":
					keyword = KeyWord.NULL;
					return true;
				
				case "RETURN":
					keyword = KeyWord.RETURN;
					return true;
					
				case "STATIC":
					keyword = KeyWord.STATIC;
					return true;
					
				case "THIS":
					keyword = KeyWord.THIS;
					return true;
					
				case "TRUE":
					keyword = KeyWord.TRUE;
					return true;
				
				case "VAR":
					keyword = KeyWord.VAR;
					return true;
			
				case "VOID":
					keyword = KeyWord.VOID;
					return true;
			
				case "WHILE":
					keyword = KeyWord.WHILE;
					return true;
				
				default:
					keyword = null;
					return false;
					
				
			}
		
	}
	public void close() throws IOException {
		System.out.println("Tokenization complete.");
		writeOut.write("</tokens>");
		writeOut.close();
	}
	
}



enum Token {
	KEYWORD, SYMBOL,
	IDENTIFIER, INT_CONST,
	STRING_CONST
}

enum KeyWord {
	CLASS, METHOD, FUNCTION,
	CONSTRUCTOR, INT,
	BOOLEAN, CHAR, VOID,
	VAR, STATIC, FIELD, LET,
	DO, IF, ELSE, WHILE,
	RETURN, TRUE, FALSE,
	NULL, THIS
}


