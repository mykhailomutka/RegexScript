package regexscript;

public enum TokenType {
  INTEGER,
  STRING,
  IDENTIFIER,
  REGEX,

  LET,
  VAR,
  FN,
  RETURN,
  IF,
  ELSE,
  WHILE,
  FOR,
  IN,
  DO,
  END,
  PRINT,
  PRINTLN,
  TRUE,
  FALSE,
  NULL,
  BREAK,
  CONTINUE,

  PLUS,
  MINUS,
  STAR,
  SLASH,
  PERCENT,

  EQUAL,
  EQEQ,
  NEQ,
  LT,
  LTE,
  GT,
  GTE,

  AND,
  OR,
  NOT,

  MATCH,
  NOT_MATCH,

  PIPE_GT,

  LPAREN,
  RPAREN,
  LBRACKET,
  RBRACKET,
  LBRACE,
  RBRACE,
  COMMA,
  COLON,
  SEMICOLON,

  EOF
}
