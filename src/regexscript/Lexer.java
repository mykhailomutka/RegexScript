package regexscript;

import java.util.ArrayList;
import java.util.List;

public final class Lexer {
  private final String src;
  private final int len;
  private int i = 0;
  private int line = 1;
  private int col = 1;

  private TokenType lastTokenType = null;

  public Lexer(String src) {
    this.src = src;
    this.len = src.length();
  }

  public List<Token> lex() {
    List<Token> out = new ArrayList<>();

    while (!isAtEnd()) {
      skipWhitespaceAndComments();
      if (isAtEnd()) break;

      int startLine = line;
      int startCol = col;
      char c = advance();

      if (c == '=' && matchChar('=')) { out.add(emit(TokenType.EQEQ, "==", startLine, startCol)); continue; }
      if (c == '!' && matchChar('=')) { out.add(emit(TokenType.NEQ, "!=", startLine, startCol)); continue; }
      if (c == '<' && matchChar('=')) { out.add(emit(TokenType.LTE, "<=", startLine, startCol)); continue; }
      if (c == '>' && matchChar('=')) { out.add(emit(TokenType.GTE, ">=", startLine, startCol)); continue; }
      if (c == '|' && matchChar('>')) { out.add(emit(TokenType.PIPE_GT, "|>", startLine, startCol)); continue; }
      if (c == '=' && matchChar('~')) { out.add(emit(TokenType.MATCH, "=~", startLine, startCol)); continue; }
      if (c == '!' && matchChar('~')) { out.add(emit(TokenType.NOT_MATCH, "!~", startLine, startCol)); continue; }

      switch (c) {
        case '+': out.add(emit(TokenType.PLUS, "+", startLine, startCol)); break;
        case '-': out.add(emit(TokenType.MINUS, "-", startLine, startCol)); break;
        case '*': out.add(emit(TokenType.STAR, "*", startLine, startCol)); break;
        case '%': out.add(emit(TokenType.PERCENT, "%", startLine, startCol)); break;
        case '=': out.add(emit(TokenType.EQUAL, "=", startLine, startCol)); break;
        case '<': out.add(emit(TokenType.LT, "<", startLine, startCol)); break;
        case '>': out.add(emit(TokenType.GT, ">", startLine, startCol)); break;

        case '(': out.add(emit(TokenType.LPAREN, "(", startLine, startCol)); break;
        case ')': out.add(emit(TokenType.RPAREN, ")", startLine, startCol)); break;
        case '[': out.add(emit(TokenType.LBRACKET, "[", startLine, startCol)); break;
        case ']': out.add(emit(TokenType.RBRACKET, "]", startLine, startCol)); break;
        case '{': out.add(emit(TokenType.LBRACE, "{", startLine, startCol)); break;
        case '}': out.add(emit(TokenType.RBRACE, "}", startLine, startCol)); break;
        case ',': out.add(emit(TokenType.COMMA, ",", startLine, startCol)); break;
        case ':': out.add(emit(TokenType.COLON, ":", startLine, startCol)); break;
        case ';': out.add(emit(TokenType.SEMICOLON, ";", startLine, startCol)); break;

        case '"':
          out.add(emit(TokenType.STRING, readString(startLine, startCol), startLine, startCol));
          break;

        case '/':
          if (canStartRegexLiteral()) {
            String rx = readRegexLiteral(startLine, startCol);
            out.add(emit(TokenType.REGEX, rx, startLine, startCol));
          } else {
            out.add(emit(TokenType.SLASH, "/", startLine, startCol));
          }
          break;

        default:
          if (isDigit(c)) {
            String num = readInteger(c);
            out.add(emit(TokenType.INTEGER, num, startLine, startCol));
          } else if (isAlpha(c) || c == '_') {
            String id = readIdentifier(c);
            TokenType kw = keywordType(id);
            out.add(emit(kw, id, startLine, startCol));
          } else {
            throw new LexError("unknown character '" + c + "'", startLine, startCol);
          }
      }
    }

    out.add(new Token(TokenType.EOF, "", line, col));
    return out;
  }

  private Token emit(TokenType type, String lexeme, int line, int col) {
    Token t = new Token(type, lexeme, line, col);
    if (type != TokenType.EOF) lastTokenType = type;
    return t;
  }

  private void skipWhitespaceAndComments() {
    while (!isAtEnd()) {
      char c = peek();
      if (c == ' ' || c == '\t' || c == '\r' || c == '\n') {
        advance();
        continue;
      }
      if (c == '#') {
        while (!isAtEnd() && peek() != '\n') advance();
        continue;
      }
      if (c == '/' && peekNext() == '/') {
        advance(); advance();
        while (!isAtEnd() && peek() != '\n') advance();
        continue;
      }
      break;
    }
  }

  private String readInteger(char first) {
    StringBuilder sb = new StringBuilder();
    sb.append(first);
    while (!isAtEnd() && isDigit(peek())) sb.append(advance());
    return sb.toString();
  }

  private String readIdentifier(char first) {
    StringBuilder sb = new StringBuilder();
    sb.append(first);
    while (!isAtEnd() && (isAlphaNum(peek()) || peek() == '_')) sb.append(advance());
    return sb.toString();
  }

  private String readString(int startLine, int startCol) {
    StringBuilder sb = new StringBuilder();
    while (!isAtEnd()) {
      char c = advance();
      if (c == '"') return sb.toString();
      if (c == '\\') {
        if (isAtEnd()) throw new LexError("unterminated escape in string", startLine, startCol);
        char e = advance();
        switch (e) {
          case 'n': sb.append('\n'); break;
          case 't': sb.append('\t'); break;
          case '"': sb.append('"'); break;
          case '\\': sb.append('\\'); break;
          default: sb.append(e); break;
        }
      } else {
        sb.append(c);
      }
    }
    throw new LexError("unterminated string literal", startLine, startCol);
  }

  private boolean canStartRegexLiteral() {
    if (lastTokenType == null) return true;
    switch (lastTokenType) {
      case EQUAL:
      case EQEQ:
      case NEQ:
      case LT:
      case LTE:
      case GT:
      case GTE:
      case PLUS:
      case MINUS:
      case STAR:
      case SLASH:
      case PERCENT:
      case AND:
      case OR:
      case NOT:
      case MATCH:
      case NOT_MATCH:
      case PIPE_GT:
      case LPAREN:
      case LBRACKET:
      case LBRACE:
      case COMMA:
      case COLON:
      case SEMICOLON:
      case DO:
      case RETURN:
      case LET:
      case VAR:
      case IF:
      case WHILE:
      case FOR:
      case IN:
      case PRINT:
      case PRINTLN:
        return true;
      default:
        return false;
    }
  }

  private String readRegexLiteral(int startLine, int startCol) {
    StringBuilder pat = new StringBuilder();
    boolean escaped = false;
    while (!isAtEnd()) {
      char c = advance();
      if (!escaped && c == '/') {
        StringBuilder flags = new StringBuilder();
        while (!isAtEnd() && Character.isLetter(peek())) flags.append(advance());
        return pat.toString() + "/" + flags.toString();
      }
      if (!escaped && c == '\\') {
        escaped = true;
        pat.append(c);
        continue;
      }
      escaped = false;
      if (c == '\n') throw new LexError("regex literal cannot span lines", startLine, startCol);
      pat.append(c);
    }
    throw new LexError("unterminated regex literal", startLine, startCol);
  }

  private TokenType keywordType(String id) {
    switch (id) {
      case "let": return TokenType.LET;
      case "var": return TokenType.VAR;
      case "fn": return TokenType.FN;
      case "return": return TokenType.RETURN;
      case "if": return TokenType.IF;
      case "else": return TokenType.ELSE;
      case "while": return TokenType.WHILE;
      case "for": return TokenType.FOR;
      case "in": return TokenType.IN;
      case "do": return TokenType.DO;
      case "end": return TokenType.END;
      case "print": return TokenType.PRINT;
      case "println": return TokenType.PRINTLN;
      case "true": return TokenType.TRUE;
      case "false": return TokenType.FALSE;
      case "null": return TokenType.NULL;
      case "break": return TokenType.BREAK;
      case "continue": return TokenType.CONTINUE;
      case "and": return TokenType.AND;
      case "or": return TokenType.OR;
      case "not": return TokenType.NOT;
      default: return TokenType.IDENTIFIER;
    }
  }

  private boolean matchChar(char expected) {
    if (isAtEnd()) return false;
    if (src.charAt(i) != expected) return false;
    advance();
    return true;
  }

  private boolean isAtEnd() { return i >= len; }
  private char peek() { return src.charAt(i); }
  private char peekNext() { return (i + 1 >= len) ? '\0' : src.charAt(i + 1); }

  private char advance() {
    char c = src.charAt(i++);
    if (c == '\n') { line += 1; col = 1; }
    else col += 1;
    return c;
  }

  private static boolean isDigit(char c) { return c >= '0' && c <= '9'; }
  private static boolean isAlpha(char c) { return (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z'); }
  private static boolean isAlphaNum(char c) { return isAlpha(c) || isDigit(c); }
}
