package regexscript;

import java.util.ArrayList;
import java.util.List;

import regexscript.Ast.*;

public final class Parser {
  private final List<Token> tokens;
  private int current = 0;

  public Parser(List<Token> tokens) { this.tokens = tokens; }

  public Program parseProgram() {
    List<Stmt> stmts = new ArrayList<>();
    while (!check(TokenType.EOF)) stmts.add(parseStmt());
    Token eof = consume(TokenType.EOF, "Expected EOF");
    return new Program(stmts, eof);
  }

  private Stmt parseStmt() {
    if (match(TokenType.LET)) return parseLet(previous());
    if (match(TokenType.VAR)) return parseVar(previous());
    if (match(TokenType.PRINT)) return parsePrint(previous(), false);
    if (match(TokenType.PRINTLN)) return parsePrint(previous(), true);
    if (match(TokenType.RETURN)) return parseReturn(previous());
    if (match(TokenType.BREAK)) { Token at = previous(); consume(TokenType.SEMICOLON, "Expected ';' after break"); return new BreakStmt(at); }
    if (match(TokenType.CONTINUE)) { Token at = previous(); consume(TokenType.SEMICOLON, "Expected ';' after continue"); return new ContinueStmt(at); }
    if (match(TokenType.IF)) return parseIf(previous());
    if (match(TokenType.WHILE)) return parseWhile(previous());
    if (match(TokenType.FOR)) return parseFor(previous());
    if (match(TokenType.FN)) return parseFn(previous());

    if (check(TokenType.IDENTIFIER) && peekNext().type == TokenType.EQUAL) return parseAssign();
    throw new ParseError("Expected statement", peek());
  }

  private Stmt parseLet(Token at) {
    Token name = consume(TokenType.IDENTIFIER, "Expected identifier after let");
    consume(TokenType.EQUAL, "Expected '=' in let");
    Expr expr = parseExpr();
    consume(TokenType.SEMICOLON, "Expected ';' after let declaration");
    return new LetStmt(name.lexeme, expr, at);
  }

  private Stmt parseVar(Token at) {
    Token name = consume(TokenType.IDENTIFIER, "Expected identifier after var");
    consume(TokenType.EQUAL, "Expected '=' in var");
    Expr expr = parseExpr();
    consume(TokenType.SEMICOLON, "Expected ';' after var declaration");
    return new VarStmt(name.lexeme, expr, at);
  }

  private Stmt parseAssign() {
    Token name = consume(TokenType.IDENTIFIER, "Expected identifier");
    Token at = name;
    consume(TokenType.EQUAL, "Expected '=' in assignment");
    Expr expr = parseExpr();
    consume(TokenType.SEMICOLON, "Expected ';' after assignment");
    return new AssignStmt(name.lexeme, expr, at);
  }

  private Stmt parsePrint(Token at, boolean newline) {
    Expr expr = parseExpr();
    consume(TokenType.SEMICOLON, "Expected ';' after print");
    return new PrintStmt(expr, newline, at);
  }

  private Stmt parseReturn(Token at) {
    if (check(TokenType.SEMICOLON)) {
      consume(TokenType.SEMICOLON, "Expected ';'");
      return new ReturnStmt(null, at);
    }
    Expr expr = parseExpr();
    consume(TokenType.SEMICOLON, "Expected ';' after return");
    return new ReturnStmt(expr, at);
  }

  private Stmt parseIf(Token at) {
    Expr cond = parseExpr();
    consume(TokenType.DO, "Expected 'do' after if condition");
    List<Stmt> thenBlock = parseBlock();
    List<Stmt> elseBlock = null;
    if (match(TokenType.ELSE)) {
      consume(TokenType.DO, "Expected 'do' after else");
      elseBlock = parseBlock();
    }
    consume(TokenType.END, "Expected 'end' to close if");
    return new IfStmt(cond, thenBlock, elseBlock, at);
  }

  private Stmt parseWhile(Token at) {
    Expr cond = parseExpr();
    consume(TokenType.DO, "Expected 'do' after while condition");
    List<Stmt> body = parseBlock();
    consume(TokenType.END, "Expected 'end' to close while");
    return new WhileStmt(cond, body, at);
  }

  private Stmt parseFor(Token at) {
    Token varName = consume(TokenType.IDENTIFIER, "Expected loop variable name");
    consume(TokenType.IN, "Expected 'in' in for loop");
    Expr iterable = parseExpr();
    consume(TokenType.DO, "Expected 'do' after for header");
    List<Stmt> body = parseBlock();
    consume(TokenType.END, "Expected 'end' to close for");
    return new ForStmt(varName.lexeme, iterable, body, at);
  }

  private Stmt parseFn(Token at) {
    Token name = consume(TokenType.IDENTIFIER, "Expected function name");
    consume(TokenType.LPAREN, "Expected '(' after function name");
    List<String> params = new ArrayList<>();
    if (!check(TokenType.RPAREN)) {
      Token p = consume(TokenType.IDENTIFIER, "Expected parameter name");
      params.add(p.lexeme);
      while (match(TokenType.COMMA)) {
        Token p2 = consume(TokenType.IDENTIFIER, "Expected parameter name");
        params.add(p2.lexeme);
      }
    }
    consume(TokenType.RPAREN, "Expected ')'");
    consume(TokenType.DO, "Expected 'do' before function body");
    List<Stmt> body = parseBlock();
    consume(TokenType.END, "Expected 'end' to close function");
    return new FnStmt(name.lexeme, params, body, at);
  }

  private List<Stmt> parseBlock() {
    List<Stmt> stmts = new ArrayList<>();
    while (!check(TokenType.END) && !check(TokenType.ELSE) && !check(TokenType.EOF)) {
      stmts.add(parseStmt());
    }
    return stmts;
  }

  // Expressions
  private Expr parseExpr() {
    Expr expr = parseOr();
    while (match(TokenType.PIPE_GT)) {
      Token op = previous();
      Expr into = parseOr();
      expr = new PipelineExpr(expr, into, op);
    }
    return expr;
  }

  private Expr parseOr() {
    Expr expr = parseAnd();
    while (match(TokenType.OR)) {
      Token op = previous();
      Expr right = parseAnd();
      expr = new BinaryExpr(expr, op, right, op);
    }
    return expr;
  }

  private Expr parseAnd() {
    Expr expr = parseRegexTest();
    while (match(TokenType.AND)) {
      Token op = previous();
      Expr right = parseRegexTest();
      expr = new BinaryExpr(expr, op, right, op);
    }
    return expr;
  }

  private Expr parseRegexTest() {
    Expr expr = parseEquality();
    while (match(TokenType.MATCH, TokenType.NOT_MATCH)) {
      Token op = previous();
      Expr right = parseEquality();
      expr = new BinaryExpr(expr, op, right, op);
    }
    return expr;
  }

  private Expr parseEquality() {
    Expr expr = parseComparison();
    while (match(TokenType.EQEQ, TokenType.NEQ)) {
      Token op = previous();
      Expr right = parseComparison();
      expr = new BinaryExpr(expr, op, right, op);
    }
    return expr;
  }

  private Expr parseComparison() {
    Expr expr = parseAdd();
    while (match(TokenType.LT, TokenType.LTE, TokenType.GT, TokenType.GTE)) {
      Token op = previous();
      Expr right = parseAdd();
      expr = new BinaryExpr(expr, op, right, op);
    }
    return expr;
  }

  private Expr parseAdd() {
    Expr expr = parseMul();
    while (match(TokenType.PLUS, TokenType.MINUS)) {
      Token op = previous();
      Expr right = parseMul();
      expr = new BinaryExpr(expr, op, right, op);
    }
    return expr;
  }

  private Expr parseMul() {
    Expr expr = parseUnary();
    while (match(TokenType.STAR, TokenType.SLASH, TokenType.PERCENT)) {
      Token op = previous();
      Expr right = parseUnary();
      expr = new BinaryExpr(expr, op, right, op);
    }
    return expr;
  }

  private Expr parseUnary() {
    if (match(TokenType.NOT, TokenType.MINUS)) {
      Token op = previous();
      Expr right = parseUnary();
      return new UnaryExpr(op, right, op);
    }
    return parsePostfix();
  }

  private Expr parsePostfix() {
    Expr expr = parsePrimary();
    while (true) {
      if (match(TokenType.LPAREN)) {
        Token at = previous();
        List<Expr> args = new ArrayList<>();
        if (!check(TokenType.RPAREN)) {
          args.add(parseExpr());
          while (match(TokenType.COMMA)) args.add(parseExpr());
        }
        consume(TokenType.RPAREN, "Expected ')'");
        expr = new CallExpr(expr, args, at);
        continue;
      }
      if (match(TokenType.LBRACKET)) {
        Token at = previous();
        Expr idx = parseExpr();
        consume(TokenType.RBRACKET, "Expected ']'");
        expr = new IndexExpr(expr, idx, at);
        continue;
      }
      break;
    }
    return expr;
  }

  private Expr parsePrimary() {
    if (match(TokenType.INTEGER)) {
      Token t = previous();
      try { return new IntLit(Long.parseLong(t.lexeme), t); }
      catch (NumberFormatException e) { throw new ParseError("Invalid integer literal", t); }
    }
    if (match(TokenType.STRING)) {
      Token t = previous();
      return new StringLit(t.lexeme, t);
    }
    if (match(TokenType.TRUE)) return new BoolLit(true, previous());
    if (match(TokenType.FALSE)) return new BoolLit(false, previous());
    if (match(TokenType.NULL)) return new NullLit(previous());

    if (match(TokenType.REGEX)) {
      Token t = previous();
      String lex = t.lexeme;
      int slash = lex.lastIndexOf('/');
      String pat = (slash >= 0) ? lex.substring(0, slash) : lex;
      String flags = (slash >= 0) ? lex.substring(slash + 1) : "";
      return new RegexLit(pat, flags, t);
    }

    if (match(TokenType.IDENTIFIER)) {
      Token t = previous();
      return new Identifier(t.lexeme, t);
    }

    if (match(TokenType.LBRACKET)) {
      Token at = previous();
      List<Expr> elements = new ArrayList<>();
      if (!check(TokenType.RBRACKET)) {
        elements.add(parseExpr());
        while (match(TokenType.COMMA)) elements.add(parseExpr());
      }
      consume(TokenType.RBRACKET, "Expected ']'");
      return new ListLit(elements, at);
    }

    if (match(TokenType.LBRACE)) {
      Token at = previous();
      List<String> keys = new ArrayList<>();
      List<Expr> vals = new ArrayList<>();
      if (!check(TokenType.RBRACE)) {
        parseMapEntry(keys, vals);
        while (match(TokenType.COMMA)) parseMapEntry(keys, vals);
      }
      consume(TokenType.RBRACE, "Expected '}'");
      return new MapLit(keys, vals, at);
    }

    if (match(TokenType.LPAREN)) {
      Expr expr = parseExpr();
      consume(TokenType.RPAREN, "Expected ')'");
      return expr;
    }

    throw new ParseError("Expected expression", peek());
  }

  private void parseMapEntry(List<String> keys, List<Expr> vals) {
    if (!match(TokenType.STRING)) throw new ParseError("Map keys must be string literals", peek());
    Token keyTok = previous();
    consume(TokenType.COLON, "Expected ':' after map key");
    Expr value = parseExpr();
    keys.add(keyTok.lexeme);
    vals.add(value);
  }

  private boolean match(TokenType... types) {
    for (TokenType t : types) {
      if (check(t)) { advance(); return true; }
    }
    return false;
  }

  private boolean check(TokenType t) { return peek().type == t; }

  private Token advance() {
    if (!check(TokenType.EOF)) current++;
    return previous();
  }

  private Token consume(TokenType t, String msg) {
    if (check(t)) return advance();
    throw new ParseError(msg, peek());
  }

  private Token peek() { return tokens.get(current); }
  private Token peekNext() { return (current + 1 >= tokens.size()) ? tokens.get(tokens.size() - 1) : tokens.get(current + 1); }
  private Token previous() { return tokens.get(current - 1); }
}
