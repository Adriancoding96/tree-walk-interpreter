package com.adrian.app;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.adrian.app.TokenType.*;

/* Scanner.java
 *
 * Walks through source code, creating and adding tokens
 * until the source code runs out of characters, when that
 * happens it appends a "end of file" token
 *
 */

public class Scanner {
  private final String source;
  private final List<Token> tokens = new ArrayList<>();
  private int start = 0;
  private int current = 0;
  private int line = 1;

  private static final Map<String, TokenType> keywords;

  static {
    keywords = new HashMap<>();
    keywords.put("and", AND);
    keywords.put("class", CLASS);
    keywords.put("else", ELSE);
    keywords.put("false", FALSE);
    keywords.put("for", FOR);
    keywords.put("fun", FUN);
    keywords.put("if", IF);
    keywords.put("nil", NIL);
    keywords.put("or", OR);
    keywords.put("print", PRINT);
    keywords.put("return", RETURN);
    keywords.put("super", SUPER);
    keywords.put("true", TRUE);
    keywords.put("var", VAR);
    keywords.put("while", WHILE);
  }

  public Scanner(String source) {
    this.source = source;
  }

  /*
   * scanTokens()
   *
   * Runs scanner until it reaches
   * the end of the source code
   *
   */

  public List<Token> scanTokens() {
    while(!isAtEnd()) {
      start = current;
      scanToken();
    }

    tokens.add(new Token(EOF, "", null, line));
    return tokens;
  }

  /*
   * scanToken()
   *
   * Recognizes lexemes by single characters, character combinations
   * and string literals
   *
   */

  private void scanToken() {
    char c = advance();
    switch (c) {
      case '(': addToken(LEFT_PAREN); break;
      case ')': addToken(RIGHT_PAREN); break;
      case '{': addToken(LEFT_BRACE); break;
      case '}': addToken(RIGHT_BRACE); break;
      case ',': addToken(COMMA); break;
      case '.': addToken(DOT); break;
      case '-': addToken(MINUS); break;
      case '+': addToken(PLUS); break;
      case ';': addToken(SEMICOLON); break;
      case '*': addToken(STAR); break;
      case '!':
        addToken(match('=') ? BANG_EQUAL : BANG);
        break;
      case '=':
        addToken(match('=') ? EQUAL_EQUAL : EQUAL);
        break;
      case '<':
        addToken(match('=') ? LESS_EQUAL : LESS);
        break;
      case '>':
        addToken(match('=') ? GREATER_EQUAL : GREATER);
        break;
      case '/':
        if(match('/')) {
          while(peek() != '\n' && !isAtEnd()) advance();
        } else {
          addToken(SLASH);
        }
        break;
      //Ignore whitespace
      case ' ':
      case '\r':
      case '\t':
        break;
      case '"': string(); break;
      default:
        /*
         * Regognizing number lexemes is done in default case to reduce
         * amount of cases needed for every decimal digit
        */
        if(isDigit(c)) {
          number();
        } else if(isAlpha(c)) {
          /*
           * Assumes any lexeme starting with a letter or underscore is
           * a identifier
           */
        } else {
          App.error(line, "Unexpected character.");
        }
        break;
    }
  }

  /*
   * identifier()
   *
   * Checks if lexeme is a keyword defined in the map keywords,
   * if not it is considered a identifier.
   */
  private void identifier() {
    while(isAlphaNumeric(peek())) advance();
    
    String text = source.substring(start, current);
    TokenType type = keywords.get(text);
    if(type == null) type = IDENTIFIER;
    addToken(type);
  }

  /*
   * number()
   *
   * Consumes digits until next character is not a digit.
   * Also looks for a fractional, if encountered start consuming
   * digits until a none digit character is found.
   *
   */
  private void number() {
    while(isDigit(peek())) advance();

    //Look for franctional
    if(peek() == '.' && isDigit(peekNext())) {
      //Consume the .
      advance();

      while(isDigit(peek())) advance();
    }

    addToken(NUMBER, Double.parseDouble(source.substring(start, current)));
  }

  /*
   * string()
   *
   * Recognises an string lexeme by consuming
   * characters until reaching closing quote.
   *
   */
  private void string() {
    while(peek() != '"' && !isAtEnd()) {
      if(peek() == '\n') line++;
      advance();
    }

    if(isAtEnd()) {
      App.error(line, "Unterminated string.");
      return;
    }

    //The closing ".
    advance();

    //Trim surrounding quotes.
    String value = source.substring(start + 1, current - 1);
    addToken(STRING, value);
  }

  /*
   * match()
   *
   * Checks the next character to determine if it
   * is a two character lexeme.
   *
   */
  private boolean match(char expected) {
    if(isAtEnd()) return false;
    if(source.charAt(current) != expected) return false;

    current++;
    return true;
  }

  /*
   * peek()
   *
   * Checks and returns next character in source code.
   */
  private char peek() {
    if(isAtEnd()) return '\0';
    return source.charAt(current);
  }

  /*
   * peekNext()
   *
   * checks and returns two characters ahead in source code.
   */
  private char peekNext() {
    if(current + 1 >= source.length()) return '\0';
    return source.charAt(current + 1);
  }

  /*
   * isAlphaNumeric()
   *
   * Checks if character is a digit or alphabetic including _
   *
   */
  private boolean isAlphaNumeric(char c) {
    return isAlpha(c) || isDigit(c);
  }

  /*
   * isAlpha()
   *
   * cheks if character is alphabetical or the character _
   * 
   */
  private boolean isAlpha(char c) {
    return (c >= 'a' && c <= 'z') ||
           (c >= 'A' && c <= 'Z') ||
            c == '_';
  }

  /*
   * isDigit()
   *
   * Determines if character is a number.
   */
  private boolean isDigit(char c) {
    return c >= '0' && c <= '9';
  }

  /*
   * isAtEnd()
   *
   * Determines if end of source code has been reached.
   */
  private boolean isAtEnd() {
    return current >= source.length();
  }

  /*
   * advance()
   *
   * Consumes next xharacter in source file
   * and returns it.
   *
   */
  private char advance() {
    return source.charAt(current++);
  }

  /*
   * addToken()
   *
   * Creates token based on the current lexeme.
   */

  private void addToken(TokenType type) {
    addToken(type, null);
  }

  private void addToken(TokenType type, Object literal) {
    String text = source.substring(start, current);
    tokens.add(new Token(type, text, literal, line));
  }

}
