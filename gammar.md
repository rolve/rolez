# Rolez Grammar

Here is the grammar for the Rolez language. This description is a manually simplified
version of the [Xtext file](ch.trick17.rolez/src/ch/trick17/rolez/Rolez.xtext) that
is used to generate the Rolez parser.

```dart
Program:
    ('package' QualifiedName)?
    Import*
    Class*;

Import:
    'import' QualifiedName '.*'?;

QualifiedName:
    ID ('.' ID)*;

Class:
    NormalClass | SingletonClass;

NormalClass:
    ('pure')? 'class' QualifiedName
    ('[' ID ']')?
    ('mapped' 'to' QualifiedName)?
    ('extends' ClassRef)? ('{'
        (Constr | Member | Slice)*
    '}')?;

SingletonClass:
    'object' QualifiedName
    ('mapped' 'to' QualifiedName)?
    ('extends' ClassRef)? ('{'
        Member*
    '}')?;

Slice:
    'slice' ID '{'
        Member*
    '}';

Member:
    Field | Method;

Constr:
    'mapped'? 'new' ('(' (Param (',' Param)*)? ')')? Block?;

Field:
    'mapped'? ('val' | 'var') ID ':' Type ('=' Expr)?;

Method:
    'mapped'?
    (('async'? 'def') | 'task' | 'override') Role ID
    ('[' (RoleParam (',' RoleParam)*)? ']')?
    ('(' (Param (',' Param)*)? ')')?
    ':' Type
    Block?;

RoleParam:
    ID ('includes' BuiltInRole)?;

Param:
    ID ':' Type;



/* Statements */

Stmt:
    Block | LocalVarDecl | IfStmt | WhileLoop | ForLoop
    | SuperConstrCall | Return | ExprStmt | ParallelStmt | Parfor;

Block:
    '{' (Stmt)* '}';

ParallelStmt:
    'parallel' Stmt 'and' Stmt;

LocalVarDecl:
    LocalVar ('=' Expr)? ';';

LocalVar:
    ('val' | 'var') ID (':' Type)?;

IfStmt:
    'if' '(' Expr ')' Stmt
    ('else' Stmt)?;

WhileLoop:
    'while' '(' Expr ')' Stmt;

ForLoop:
    'for' '(' LocalVarDecl Expr ';' Expr ')' Stmt;

Parfor:
    'parfor' '(' LocalVarDecl Expr ';' Expr ')' Stmt;

SuperConstrCall:
    'super' '(' Expr (',' Expr)* ')' ';';
    // Note that parentheses are required here. A "super" without parentheses is an expression.

Return:
    'return' Expr? ';';

ExprStmt:
    Expr ';';



/* Expressions */

Expr:
    Assignment;

Assignment:
    OrExpr (('=' | '|=' | '&=' | '+=' | '-=' | '*=' | '/=' | '%=') Expr)?;

OrExpr:
    AndExpr ('||' AndExpr)*;

AndExpr:
    BitwiseOrExpr ('&&' BitwiseOrExpr)*;

BitwiseOrExpr:
    BitwiseXorExpr ('|' BitwiseXorExpr)*;

BitwiseXorExpr:
    BitwiseAndExpr ('^' BitwiseAndExpr)*;

BitwiseAndExpr:
    EqualityExpr ('&' EqualityExpr)*;

EqualityExpr:
    RelationalExpr ('==' RelationalExpr)?;

RelationalExpr:
    ShiftExpr (('<' | '>' | '<=' | '>=') ShiftExpr)?;

ShiftExpr: 
    AdditiveExpr (('<<' | '>>' |  '>>>') AdditiveExpr)*;

AdditiveExpr:
    MultiplicativeExpr (('+' | '-') MultiplicativeExpr)*;

MultiplicativeExpr:
    Cast (('*' | '/' | '%') Cast)*;

Cast:
    ArithmeticUnaryPreExpr ('as' Type)*;

ArithmeticUnaryPreExpr:
    LogicalNot | ('-' | '++' | '--') LogicalNot;

LogicalNot:
    BitwiseNot | '!' BitwiseNot;

BitwiseNot:
    ArithmeticUnaryPostExpr | '~' ArithmeticUnaryPostExpr;

ArithmeticUnaryPostExpr:
    Slicing ('++' | '--')?;

Slicing:
    Start ('slice' ID)?;

Start:
    MemberAccess (
        'start' ID
        ('['  Role (',' Role)*   ']')?
        ('(' (Expr (',' Expr)*)? ')')?
    )*;

MemberAccess:
    SimpleExpr (
        '.' ID
        ('['  Role (',' Role)*   ']')?
        ('(' (Expr (',' Expr)*)? ')')?
    )*;

SimpleExpr:
    'this' | 'super' | Ref | New | Literal | '(' Expr ')';

Ref:
    ID | '(' QualifiedName ')';

New:
    'new' NewClassRef ('(' (Expr (',' Expr)*)? ')')?;

Literal:
    DOUBLE | LONG | INT | BOOL | CHAR | STRING | 'null';



/* Types */

Type:
    PrimitiveType | ReferenceTypeOrTypeParamRef;

PrimitiveType:
    'double' | 'long' | 'int' | 'short' | 'byte'
    | 'char' | 'boolean' | 'void'?;

ReferenceTypeOrTypeParamRef:
    'Null' | ID | ExplicitRoleType | GenericRoleType | RestrictedTypeParamRef;

ExplicitRoleType:
    Role ClassRef ('\\' ID)?;

GenericRoleType:
    QualifiedName '[' Type ']';

RestrictedTypeParamRef:
    ID 'with' Role;

Role:
    ID | BuiltInRole;

BuiltInRole:
    'pure' | 'readonly' | 'readwrite';

ClassRef:
    QualifiedName ('[' Type ']')?;

NewClassRef:
    (ID | '(' QualifiedName ')') ('[' Type ']')?;
```
