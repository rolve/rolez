package ch.trick17.rolez.lang.ui.contentassist.antlr.internal;

// Hack: Use our own Lexer superclass by means of import. 
// Currently there is no other way to specify the superclass for the lexer.
import org.eclipse.xtext.ui.editor.contentassist.antlr.internal.Lexer;


import org.antlr.runtime.*;
import java.util.Stack;
import java.util.List;
import java.util.ArrayList;

@SuppressWarnings("all")
public class InternalRolezLexer extends Lexer {
    public static final int T__68=68;
    public static final int T__69=69;
    public static final int T__66=66;
    public static final int RULE_ID=4;
    public static final int T__67=67;
    public static final int T__29=29;
    public static final int T__64=64;
    public static final int T__28=28;
    public static final int T__65=65;
    public static final int T__27=27;
    public static final int T__62=62;
    public static final int T__26=26;
    public static final int T__63=63;
    public static final int T__25=25;
    public static final int T__24=24;
    public static final int T__23=23;
    public static final int T__22=22;
    public static final int RULE_ANY_OTHER=13;
    public static final int T__21=21;
    public static final int T__20=20;
    public static final int T__61=61;
    public static final int EOF=-1;
    public static final int T__60=60;
    public static final int T__55=55;
    public static final int T__56=56;
    public static final int T__19=19;
    public static final int T__57=57;
    public static final int T__58=58;
    public static final int T__16=16;
    public static final int T__51=51;
    public static final int T__15=15;
    public static final int T__52=52;
    public static final int T__53=53;
    public static final int T__18=18;
    public static final int T__54=54;
    public static final int T__17=17;
    public static final int T__14=14;
    public static final int T__59=59;
    public static final int RULE_INT=7;
    public static final int RULE_CHAR=9;
    public static final int T__50=50;
    public static final int RULE_NULL_TYPE=5;
    public static final int T__42=42;
    public static final int T__43=43;
    public static final int T__40=40;
    public static final int T__41=41;
    public static final int T__46=46;
    public static final int T__47=47;
    public static final int T__44=44;
    public static final int T__45=45;
    public static final int T__48=48;
    public static final int T__49=49;
    public static final int RULE_SL_COMMENT=11;
    public static final int RULE_DOUBLE=8;
    public static final int RULE_ML_COMMENT=10;
    public static final int T__30=30;
    public static final int T__31=31;
    public static final int T__32=32;
    public static final int RULE_STRING=6;
    public static final int T__33=33;
    public static final int T__71=71;
    public static final int T__34=34;
    public static final int T__35=35;
    public static final int T__36=36;
    public static final int T__70=70;
    public static final int T__37=37;
    public static final int T__38=38;
    public static final int T__39=39;
    public static final int RULE_WS=12;

    // delegates
    // delegators

    public InternalRolezLexer() {;} 
    public InternalRolezLexer(CharStream input) {
        this(input, new RecognizerSharedState());
    }
    public InternalRolezLexer(CharStream input, RecognizerSharedState state) {
        super(input,state);

    }
    public String getGrammarFileName() { return "ch.trick17.rolez.lang.ui/ch.trick17.rolez.lang/target/xtext-gen/ch/trick17/rolez/lang/ui/contentassist/antlr/internal/InternalRolez.g"; }

    // $ANTLR start "T__14"
    public final void mT__14() throws RecognitionException {
        try {
            int _type = T__14;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // ch.trick17.rolez.lang.ui/ch.trick17.rolez.lang/target/xtext-gen/ch/trick17/rolez/lang/ui/contentassist/antlr/internal/InternalRolez.g:11:7: ( 'val' )
            // ch.trick17.rolez.lang.ui/ch.trick17.rolez.lang/target/xtext-gen/ch/trick17/rolez/lang/ui/contentassist/antlr/internal/InternalRolez.g:11:9: 'val'
            {
            match("val"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__14"

    // $ANTLR start "T__15"
    public final void mT__15() throws RecognitionException {
        try {
            int _type = T__15;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // ch.trick17.rolez.lang.ui/ch.trick17.rolez.lang/target/xtext-gen/ch/trick17/rolez/lang/ui/contentassist/antlr/internal/InternalRolez.g:12:7: ( '||' )
            // ch.trick17.rolez.lang.ui/ch.trick17.rolez.lang/target/xtext-gen/ch/trick17/rolez/lang/ui/contentassist/antlr/internal/InternalRolez.g:12:9: '||'
            {
            match("||"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__15"

    // $ANTLR start "T__16"
    public final void mT__16() throws RecognitionException {
        try {
            int _type = T__16;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // ch.trick17.rolez.lang.ui/ch.trick17.rolez.lang/target/xtext-gen/ch/trick17/rolez/lang/ui/contentassist/antlr/internal/InternalRolez.g:13:7: ( '&&' )
            // ch.trick17.rolez.lang.ui/ch.trick17.rolez.lang/target/xtext-gen/ch/trick17/rolez/lang/ui/contentassist/antlr/internal/InternalRolez.g:13:9: '&&'
            {
            match("&&"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__16"

    // $ANTLR start "T__17"
    public final void mT__17() throws RecognitionException {
        try {
            int _type = T__17;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // ch.trick17.rolez.lang.ui/ch.trick17.rolez.lang/target/xtext-gen/ch/trick17/rolez/lang/ui/contentassist/antlr/internal/InternalRolez.g:14:7: ( 'def' )
            // ch.trick17.rolez.lang.ui/ch.trick17.rolez.lang/target/xtext-gen/ch/trick17/rolez/lang/ui/contentassist/antlr/internal/InternalRolez.g:14:9: 'def'
            {
            match("def"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__17"

    // $ANTLR start "T__18"
    public final void mT__18() throws RecognitionException {
        try {
            int _type = T__18;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // ch.trick17.rolez.lang.ui/ch.trick17.rolez.lang/target/xtext-gen/ch/trick17/rolez/lang/ui/contentassist/antlr/internal/InternalRolez.g:15:7: ( 'true' )
            // ch.trick17.rolez.lang.ui/ch.trick17.rolez.lang/target/xtext-gen/ch/trick17/rolez/lang/ui/contentassist/antlr/internal/InternalRolez.g:15:9: 'true'
            {
            match("true"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__18"

    // $ANTLR start "T__19"
    public final void mT__19() throws RecognitionException {
        try {
            int _type = T__19;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // ch.trick17.rolez.lang.ui/ch.trick17.rolez.lang/target/xtext-gen/ch/trick17/rolez/lang/ui/contentassist/antlr/internal/InternalRolez.g:16:7: ( 'false' )
            // ch.trick17.rolez.lang.ui/ch.trick17.rolez.lang/target/xtext-gen/ch/trick17/rolez/lang/ui/contentassist/antlr/internal/InternalRolez.g:16:9: 'false'
            {
            match("false"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__19"

    // $ANTLR start "T__20"
    public final void mT__20() throws RecognitionException {
        try {
            int _type = T__20;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // ch.trick17.rolez.lang.ui/ch.trick17.rolez.lang/target/xtext-gen/ch/trick17/rolez/lang/ui/contentassist/antlr/internal/InternalRolez.g:17:7: ( 'var' )
            // ch.trick17.rolez.lang.ui/ch.trick17.rolez.lang/target/xtext-gen/ch/trick17/rolez/lang/ui/contentassist/antlr/internal/InternalRolez.g:17:9: 'var'
            {
            match("var"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__20"

    // $ANTLR start "T__21"
    public final void mT__21() throws RecognitionException {
        try {
            int _type = T__21;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // ch.trick17.rolez.lang.ui/ch.trick17.rolez.lang/target/xtext-gen/ch/trick17/rolez/lang/ui/contentassist/antlr/internal/InternalRolez.g:18:7: ( '==' )
            // ch.trick17.rolez.lang.ui/ch.trick17.rolez.lang/target/xtext-gen/ch/trick17/rolez/lang/ui/contentassist/antlr/internal/InternalRolez.g:18:9: '=='
            {
            match("=="); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__21"

    // $ANTLR start "T__22"
    public final void mT__22() throws RecognitionException {
        try {
            int _type = T__22;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // ch.trick17.rolez.lang.ui/ch.trick17.rolez.lang/target/xtext-gen/ch/trick17/rolez/lang/ui/contentassist/antlr/internal/InternalRolez.g:19:7: ( '!=' )
            // ch.trick17.rolez.lang.ui/ch.trick17.rolez.lang/target/xtext-gen/ch/trick17/rolez/lang/ui/contentassist/antlr/internal/InternalRolez.g:19:9: '!='
            {
            match("!="); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__22"

    // $ANTLR start "T__23"
    public final void mT__23() throws RecognitionException {
        try {
            int _type = T__23;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // ch.trick17.rolez.lang.ui/ch.trick17.rolez.lang/target/xtext-gen/ch/trick17/rolez/lang/ui/contentassist/antlr/internal/InternalRolez.g:20:7: ( '<' )
            // ch.trick17.rolez.lang.ui/ch.trick17.rolez.lang/target/xtext-gen/ch/trick17/rolez/lang/ui/contentassist/antlr/internal/InternalRolez.g:20:9: '<'
            {
            match('<'); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__23"

    // $ANTLR start "T__24"
    public final void mT__24() throws RecognitionException {
        try {
            int _type = T__24;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // ch.trick17.rolez.lang.ui/ch.trick17.rolez.lang/target/xtext-gen/ch/trick17/rolez/lang/ui/contentassist/antlr/internal/InternalRolez.g:21:7: ( '>' )
            // ch.trick17.rolez.lang.ui/ch.trick17.rolez.lang/target/xtext-gen/ch/trick17/rolez/lang/ui/contentassist/antlr/internal/InternalRolez.g:21:9: '>'
            {
            match('>'); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__24"

    // $ANTLR start "T__25"
    public final void mT__25() throws RecognitionException {
        try {
            int _type = T__25;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // ch.trick17.rolez.lang.ui/ch.trick17.rolez.lang/target/xtext-gen/ch/trick17/rolez/lang/ui/contentassist/antlr/internal/InternalRolez.g:22:7: ( '<=' )
            // ch.trick17.rolez.lang.ui/ch.trick17.rolez.lang/target/xtext-gen/ch/trick17/rolez/lang/ui/contentassist/antlr/internal/InternalRolez.g:22:9: '<='
            {
            match("<="); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__25"

    // $ANTLR start "T__26"
    public final void mT__26() throws RecognitionException {
        try {
            int _type = T__26;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // ch.trick17.rolez.lang.ui/ch.trick17.rolez.lang/target/xtext-gen/ch/trick17/rolez/lang/ui/contentassist/antlr/internal/InternalRolez.g:23:7: ( '>=' )
            // ch.trick17.rolez.lang.ui/ch.trick17.rolez.lang/target/xtext-gen/ch/trick17/rolez/lang/ui/contentassist/antlr/internal/InternalRolez.g:23:9: '>='
            {
            match(">="); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__26"

    // $ANTLR start "T__27"
    public final void mT__27() throws RecognitionException {
        try {
            int _type = T__27;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // ch.trick17.rolez.lang.ui/ch.trick17.rolez.lang/target/xtext-gen/ch/trick17/rolez/lang/ui/contentassist/antlr/internal/InternalRolez.g:24:7: ( '+' )
            // ch.trick17.rolez.lang.ui/ch.trick17.rolez.lang/target/xtext-gen/ch/trick17/rolez/lang/ui/contentassist/antlr/internal/InternalRolez.g:24:9: '+'
            {
            match('+'); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__27"

    // $ANTLR start "T__28"
    public final void mT__28() throws RecognitionException {
        try {
            int _type = T__28;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // ch.trick17.rolez.lang.ui/ch.trick17.rolez.lang/target/xtext-gen/ch/trick17/rolez/lang/ui/contentassist/antlr/internal/InternalRolez.g:25:7: ( '-' )
            // ch.trick17.rolez.lang.ui/ch.trick17.rolez.lang/target/xtext-gen/ch/trick17/rolez/lang/ui/contentassist/antlr/internal/InternalRolez.g:25:9: '-'
            {
            match('-'); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__28"

    // $ANTLR start "T__29"
    public final void mT__29() throws RecognitionException {
        try {
            int _type = T__29;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // ch.trick17.rolez.lang.ui/ch.trick17.rolez.lang/target/xtext-gen/ch/trick17/rolez/lang/ui/contentassist/antlr/internal/InternalRolez.g:26:7: ( '*' )
            // ch.trick17.rolez.lang.ui/ch.trick17.rolez.lang/target/xtext-gen/ch/trick17/rolez/lang/ui/contentassist/antlr/internal/InternalRolez.g:26:9: '*'
            {
            match('*'); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__29"

    // $ANTLR start "T__30"
    public final void mT__30() throws RecognitionException {
        try {
            int _type = T__30;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // ch.trick17.rolez.lang.ui/ch.trick17.rolez.lang/target/xtext-gen/ch/trick17/rolez/lang/ui/contentassist/antlr/internal/InternalRolez.g:27:7: ( '/' )
            // ch.trick17.rolez.lang.ui/ch.trick17.rolez.lang/target/xtext-gen/ch/trick17/rolez/lang/ui/contentassist/antlr/internal/InternalRolez.g:27:9: '/'
            {
            match('/'); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__30"

    // $ANTLR start "T__31"
    public final void mT__31() throws RecognitionException {
        try {
            int _type = T__31;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // ch.trick17.rolez.lang.ui/ch.trick17.rolez.lang/target/xtext-gen/ch/trick17/rolez/lang/ui/contentassist/antlr/internal/InternalRolez.g:28:7: ( '%' )
            // ch.trick17.rolez.lang.ui/ch.trick17.rolez.lang/target/xtext-gen/ch/trick17/rolez/lang/ui/contentassist/antlr/internal/InternalRolez.g:28:9: '%'
            {
            match('%'); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__31"

    // $ANTLR start "T__32"
    public final void mT__32() throws RecognitionException {
        try {
            int _type = T__32;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // ch.trick17.rolez.lang.ui/ch.trick17.rolez.lang/target/xtext-gen/ch/trick17/rolez/lang/ui/contentassist/antlr/internal/InternalRolez.g:29:7: ( 'readwrite' )
            // ch.trick17.rolez.lang.ui/ch.trick17.rolez.lang/target/xtext-gen/ch/trick17/rolez/lang/ui/contentassist/antlr/internal/InternalRolez.g:29:9: 'readwrite'
            {
            match("readwrite"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__32"

    // $ANTLR start "T__33"
    public final void mT__33() throws RecognitionException {
        try {
            int _type = T__33;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // ch.trick17.rolez.lang.ui/ch.trick17.rolez.lang/target/xtext-gen/ch/trick17/rolez/lang/ui/contentassist/antlr/internal/InternalRolez.g:30:7: ( 'readonly' )
            // ch.trick17.rolez.lang.ui/ch.trick17.rolez.lang/target/xtext-gen/ch/trick17/rolez/lang/ui/contentassist/antlr/internal/InternalRolez.g:30:9: 'readonly'
            {
            match("readonly"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__33"

    // $ANTLR start "T__34"
    public final void mT__34() throws RecognitionException {
        try {
            int _type = T__34;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // ch.trick17.rolez.lang.ui/ch.trick17.rolez.lang/target/xtext-gen/ch/trick17/rolez/lang/ui/contentassist/antlr/internal/InternalRolez.g:31:7: ( 'pure' )
            // ch.trick17.rolez.lang.ui/ch.trick17.rolez.lang/target/xtext-gen/ch/trick17/rolez/lang/ui/contentassist/antlr/internal/InternalRolez.g:31:9: 'pure'
            {
            match("pure"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__34"

    // $ANTLR start "T__35"
    public final void mT__35() throws RecognitionException {
        try {
            int _type = T__35;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // ch.trick17.rolez.lang.ui/ch.trick17.rolez.lang/target/xtext-gen/ch/trick17/rolez/lang/ui/contentassist/antlr/internal/InternalRolez.g:32:7: ( 'package' )
            // ch.trick17.rolez.lang.ui/ch.trick17.rolez.lang/target/xtext-gen/ch/trick17/rolez/lang/ui/contentassist/antlr/internal/InternalRolez.g:32:9: 'package'
            {
            match("package"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__35"

    // $ANTLR start "T__36"
    public final void mT__36() throws RecognitionException {
        try {
            int _type = T__36;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // ch.trick17.rolez.lang.ui/ch.trick17.rolez.lang/target/xtext-gen/ch/trick17/rolez/lang/ui/contentassist/antlr/internal/InternalRolez.g:33:7: ( 'import' )
            // ch.trick17.rolez.lang.ui/ch.trick17.rolez.lang/target/xtext-gen/ch/trick17/rolez/lang/ui/contentassist/antlr/internal/InternalRolez.g:33:9: 'import'
            {
            match("import"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__36"

    // $ANTLR start "T__37"
    public final void mT__37() throws RecognitionException {
        try {
            int _type = T__37;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // ch.trick17.rolez.lang.ui/ch.trick17.rolez.lang/target/xtext-gen/ch/trick17/rolez/lang/ui/contentassist/antlr/internal/InternalRolez.g:34:7: ( '.' )
            // ch.trick17.rolez.lang.ui/ch.trick17.rolez.lang/target/xtext-gen/ch/trick17/rolez/lang/ui/contentassist/antlr/internal/InternalRolez.g:34:9: '.'
            {
            match('.'); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__37"

    // $ANTLR start "T__38"
    public final void mT__38() throws RecognitionException {
        try {
            int _type = T__38;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // ch.trick17.rolez.lang.ui/ch.trick17.rolez.lang/target/xtext-gen/ch/trick17/rolez/lang/ui/contentassist/antlr/internal/InternalRolez.g:35:7: ( '.*' )
            // ch.trick17.rolez.lang.ui/ch.trick17.rolez.lang/target/xtext-gen/ch/trick17/rolez/lang/ui/contentassist/antlr/internal/InternalRolez.g:35:9: '.*'
            {
            match(".*"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__38"

    // $ANTLR start "T__39"
    public final void mT__39() throws RecognitionException {
        try {
            int _type = T__39;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // ch.trick17.rolez.lang.ui/ch.trick17.rolez.lang/target/xtext-gen/ch/trick17/rolez/lang/ui/contentassist/antlr/internal/InternalRolez.g:36:7: ( 'class' )
            // ch.trick17.rolez.lang.ui/ch.trick17.rolez.lang/target/xtext-gen/ch/trick17/rolez/lang/ui/contentassist/antlr/internal/InternalRolez.g:36:9: 'class'
            {
            match("class"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__39"

    // $ANTLR start "T__40"
    public final void mT__40() throws RecognitionException {
        try {
            int _type = T__40;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // ch.trick17.rolez.lang.ui/ch.trick17.rolez.lang/target/xtext-gen/ch/trick17/rolez/lang/ui/contentassist/antlr/internal/InternalRolez.g:37:7: ( '[' )
            // ch.trick17.rolez.lang.ui/ch.trick17.rolez.lang/target/xtext-gen/ch/trick17/rolez/lang/ui/contentassist/antlr/internal/InternalRolez.g:37:9: '['
            {
            match('['); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__40"

    // $ANTLR start "T__41"
    public final void mT__41() throws RecognitionException {
        try {
            int _type = T__41;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // ch.trick17.rolez.lang.ui/ch.trick17.rolez.lang/target/xtext-gen/ch/trick17/rolez/lang/ui/contentassist/antlr/internal/InternalRolez.g:38:7: ( ']' )
            // ch.trick17.rolez.lang.ui/ch.trick17.rolez.lang/target/xtext-gen/ch/trick17/rolez/lang/ui/contentassist/antlr/internal/InternalRolez.g:38:9: ']'
            {
            match(']'); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__41"

    // $ANTLR start "T__42"
    public final void mT__42() throws RecognitionException {
        try {
            int _type = T__42;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // ch.trick17.rolez.lang.ui/ch.trick17.rolez.lang/target/xtext-gen/ch/trick17/rolez/lang/ui/contentassist/antlr/internal/InternalRolez.g:39:7: ( 'extends' )
            // ch.trick17.rolez.lang.ui/ch.trick17.rolez.lang/target/xtext-gen/ch/trick17/rolez/lang/ui/contentassist/antlr/internal/InternalRolez.g:39:9: 'extends'
            {
            match("extends"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__42"

    // $ANTLR start "T__43"
    public final void mT__43() throws RecognitionException {
        try {
            int _type = T__43;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // ch.trick17.rolez.lang.ui/ch.trick17.rolez.lang/target/xtext-gen/ch/trick17/rolez/lang/ui/contentassist/antlr/internal/InternalRolez.g:40:7: ( '{' )
            // ch.trick17.rolez.lang.ui/ch.trick17.rolez.lang/target/xtext-gen/ch/trick17/rolez/lang/ui/contentassist/antlr/internal/InternalRolez.g:40:9: '{'
            {
            match('{'); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__43"

    // $ANTLR start "T__44"
    public final void mT__44() throws RecognitionException {
        try {
            int _type = T__44;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // ch.trick17.rolez.lang.ui/ch.trick17.rolez.lang/target/xtext-gen/ch/trick17/rolez/lang/ui/contentassist/antlr/internal/InternalRolez.g:41:7: ( '}' )
            // ch.trick17.rolez.lang.ui/ch.trick17.rolez.lang/target/xtext-gen/ch/trick17/rolez/lang/ui/contentassist/antlr/internal/InternalRolez.g:41:9: '}'
            {
            match('}'); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__44"

    // $ANTLR start "T__45"
    public final void mT__45() throws RecognitionException {
        try {
            int _type = T__45;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // ch.trick17.rolez.lang.ui/ch.trick17.rolez.lang/target/xtext-gen/ch/trick17/rolez/lang/ui/contentassist/antlr/internal/InternalRolez.g:42:7: ( 'object' )
            // ch.trick17.rolez.lang.ui/ch.trick17.rolez.lang/target/xtext-gen/ch/trick17/rolez/lang/ui/contentassist/antlr/internal/InternalRolez.g:42:9: 'object'
            {
            match("object"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__45"

    // $ANTLR start "T__46"
    public final void mT__46() throws RecognitionException {
        try {
            int _type = T__46;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // ch.trick17.rolez.lang.ui/ch.trick17.rolez.lang/target/xtext-gen/ch/trick17/rolez/lang/ui/contentassist/antlr/internal/InternalRolez.g:43:7: ( 'task' )
            // ch.trick17.rolez.lang.ui/ch.trick17.rolez.lang/target/xtext-gen/ch/trick17/rolez/lang/ui/contentassist/antlr/internal/InternalRolez.g:43:9: 'task'
            {
            match("task"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__46"

    // $ANTLR start "T__47"
    public final void mT__47() throws RecognitionException {
        try {
            int _type = T__47;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // ch.trick17.rolez.lang.ui/ch.trick17.rolez.lang/target/xtext-gen/ch/trick17/rolez/lang/ui/contentassist/antlr/internal/InternalRolez.g:44:7: ( ':' )
            // ch.trick17.rolez.lang.ui/ch.trick17.rolez.lang/target/xtext-gen/ch/trick17/rolez/lang/ui/contentassist/antlr/internal/InternalRolez.g:44:9: ':'
            {
            match(':'); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__47"

    // $ANTLR start "T__48"
    public final void mT__48() throws RecognitionException {
        try {
            int _type = T__48;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // ch.trick17.rolez.lang.ui/ch.trick17.rolez.lang/target/xtext-gen/ch/trick17/rolez/lang/ui/contentassist/antlr/internal/InternalRolez.g:45:7: ( '(' )
            // ch.trick17.rolez.lang.ui/ch.trick17.rolez.lang/target/xtext-gen/ch/trick17/rolez/lang/ui/contentassist/antlr/internal/InternalRolez.g:45:9: '('
            {
            match('('); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__48"

    // $ANTLR start "T__49"
    public final void mT__49() throws RecognitionException {
        try {
            int _type = T__49;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // ch.trick17.rolez.lang.ui/ch.trick17.rolez.lang/target/xtext-gen/ch/trick17/rolez/lang/ui/contentassist/antlr/internal/InternalRolez.g:46:7: ( ')' )
            // ch.trick17.rolez.lang.ui/ch.trick17.rolez.lang/target/xtext-gen/ch/trick17/rolez/lang/ui/contentassist/antlr/internal/InternalRolez.g:46:9: ')'
            {
            match(')'); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__49"

    // $ANTLR start "T__50"
    public final void mT__50() throws RecognitionException {
        try {
            int _type = T__50;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // ch.trick17.rolez.lang.ui/ch.trick17.rolez.lang/target/xtext-gen/ch/trick17/rolez/lang/ui/contentassist/antlr/internal/InternalRolez.g:47:7: ( ',' )
            // ch.trick17.rolez.lang.ui/ch.trick17.rolez.lang/target/xtext-gen/ch/trick17/rolez/lang/ui/contentassist/antlr/internal/InternalRolez.g:47:9: ','
            {
            match(','); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__50"

    // $ANTLR start "T__51"
    public final void mT__51() throws RecognitionException {
        try {
            int _type = T__51;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // ch.trick17.rolez.lang.ui/ch.trick17.rolez.lang/target/xtext-gen/ch/trick17/rolez/lang/ui/contentassist/antlr/internal/InternalRolez.g:48:7: ( 'new' )
            // ch.trick17.rolez.lang.ui/ch.trick17.rolez.lang/target/xtext-gen/ch/trick17/rolez/lang/ui/contentassist/antlr/internal/InternalRolez.g:48:9: 'new'
            {
            match("new"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__51"

    // $ANTLR start "T__52"
    public final void mT__52() throws RecognitionException {
        try {
            int _type = T__52;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // ch.trick17.rolez.lang.ui/ch.trick17.rolez.lang/target/xtext-gen/ch/trick17/rolez/lang/ui/contentassist/antlr/internal/InternalRolez.g:49:7: ( '=' )
            // ch.trick17.rolez.lang.ui/ch.trick17.rolez.lang/target/xtext-gen/ch/trick17/rolez/lang/ui/contentassist/antlr/internal/InternalRolez.g:49:9: '='
            {
            match('='); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__52"

    // $ANTLR start "T__53"
    public final void mT__53() throws RecognitionException {
        try {
            int _type = T__53;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // ch.trick17.rolez.lang.ui/ch.trick17.rolez.lang/target/xtext-gen/ch/trick17/rolez/lang/ui/contentassist/antlr/internal/InternalRolez.g:50:7: ( ';' )
            // ch.trick17.rolez.lang.ui/ch.trick17.rolez.lang/target/xtext-gen/ch/trick17/rolez/lang/ui/contentassist/antlr/internal/InternalRolez.g:50:9: ';'
            {
            match(';'); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__53"

    // $ANTLR start "T__54"
    public final void mT__54() throws RecognitionException {
        try {
            int _type = T__54;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // ch.trick17.rolez.lang.ui/ch.trick17.rolez.lang/target/xtext-gen/ch/trick17/rolez/lang/ui/contentassist/antlr/internal/InternalRolez.g:51:7: ( 'if' )
            // ch.trick17.rolez.lang.ui/ch.trick17.rolez.lang/target/xtext-gen/ch/trick17/rolez/lang/ui/contentassist/antlr/internal/InternalRolez.g:51:9: 'if'
            {
            match("if"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__54"

    // $ANTLR start "T__55"
    public final void mT__55() throws RecognitionException {
        try {
            int _type = T__55;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // ch.trick17.rolez.lang.ui/ch.trick17.rolez.lang/target/xtext-gen/ch/trick17/rolez/lang/ui/contentassist/antlr/internal/InternalRolez.g:52:7: ( 'else' )
            // ch.trick17.rolez.lang.ui/ch.trick17.rolez.lang/target/xtext-gen/ch/trick17/rolez/lang/ui/contentassist/antlr/internal/InternalRolez.g:52:9: 'else'
            {
            match("else"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__55"

    // $ANTLR start "T__56"
    public final void mT__56() throws RecognitionException {
        try {
            int _type = T__56;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // ch.trick17.rolez.lang.ui/ch.trick17.rolez.lang/target/xtext-gen/ch/trick17/rolez/lang/ui/contentassist/antlr/internal/InternalRolez.g:53:7: ( 'while' )
            // ch.trick17.rolez.lang.ui/ch.trick17.rolez.lang/target/xtext-gen/ch/trick17/rolez/lang/ui/contentassist/antlr/internal/InternalRolez.g:53:9: 'while'
            {
            match("while"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__56"

    // $ANTLR start "T__57"
    public final void mT__57() throws RecognitionException {
        try {
            int _type = T__57;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // ch.trick17.rolez.lang.ui/ch.trick17.rolez.lang/target/xtext-gen/ch/trick17/rolez/lang/ui/contentassist/antlr/internal/InternalRolez.g:54:7: ( 'super' )
            // ch.trick17.rolez.lang.ui/ch.trick17.rolez.lang/target/xtext-gen/ch/trick17/rolez/lang/ui/contentassist/antlr/internal/InternalRolez.g:54:9: 'super'
            {
            match("super"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__57"

    // $ANTLR start "T__58"
    public final void mT__58() throws RecognitionException {
        try {
            int _type = T__58;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // ch.trick17.rolez.lang.ui/ch.trick17.rolez.lang/target/xtext-gen/ch/trick17/rolez/lang/ui/contentassist/antlr/internal/InternalRolez.g:55:7: ( 'return' )
            // ch.trick17.rolez.lang.ui/ch.trick17.rolez.lang/target/xtext-gen/ch/trick17/rolez/lang/ui/contentassist/antlr/internal/InternalRolez.g:55:9: 'return'
            {
            match("return"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__58"

    // $ANTLR start "T__59"
    public final void mT__59() throws RecognitionException {
        try {
            int _type = T__59;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // ch.trick17.rolez.lang.ui/ch.trick17.rolez.lang/target/xtext-gen/ch/trick17/rolez/lang/ui/contentassist/antlr/internal/InternalRolez.g:56:7: ( 'as' )
            // ch.trick17.rolez.lang.ui/ch.trick17.rolez.lang/target/xtext-gen/ch/trick17/rolez/lang/ui/contentassist/antlr/internal/InternalRolez.g:56:9: 'as'
            {
            match("as"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__59"

    // $ANTLR start "T__60"
    public final void mT__60() throws RecognitionException {
        try {
            int _type = T__60;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // ch.trick17.rolez.lang.ui/ch.trick17.rolez.lang/target/xtext-gen/ch/trick17/rolez/lang/ui/contentassist/antlr/internal/InternalRolez.g:57:7: ( '!' )
            // ch.trick17.rolez.lang.ui/ch.trick17.rolez.lang/target/xtext-gen/ch/trick17/rolez/lang/ui/contentassist/antlr/internal/InternalRolez.g:57:9: '!'
            {
            match('!'); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__60"

    // $ANTLR start "T__61"
    public final void mT__61() throws RecognitionException {
        try {
            int _type = T__61;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // ch.trick17.rolez.lang.ui/ch.trick17.rolez.lang/target/xtext-gen/ch/trick17/rolez/lang/ui/contentassist/antlr/internal/InternalRolez.g:58:7: ( 'this' )
            // ch.trick17.rolez.lang.ui/ch.trick17.rolez.lang/target/xtext-gen/ch/trick17/rolez/lang/ui/contentassist/antlr/internal/InternalRolez.g:58:9: 'this'
            {
            match("this"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__61"

    // $ANTLR start "T__62"
    public final void mT__62() throws RecognitionException {
        try {
            int _type = T__62;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // ch.trick17.rolez.lang.ui/ch.trick17.rolez.lang/target/xtext-gen/ch/trick17/rolez/lang/ui/contentassist/antlr/internal/InternalRolez.g:59:7: ( 'the' )
            // ch.trick17.rolez.lang.ui/ch.trick17.rolez.lang/target/xtext-gen/ch/trick17/rolez/lang/ui/contentassist/antlr/internal/InternalRolez.g:59:9: 'the'
            {
            match("the"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__62"

    // $ANTLR start "T__63"
    public final void mT__63() throws RecognitionException {
        try {
            int _type = T__63;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // ch.trick17.rolez.lang.ui/ch.trick17.rolez.lang/target/xtext-gen/ch/trick17/rolez/lang/ui/contentassist/antlr/internal/InternalRolez.g:60:7: ( 'start' )
            // ch.trick17.rolez.lang.ui/ch.trick17.rolez.lang/target/xtext-gen/ch/trick17/rolez/lang/ui/contentassist/antlr/internal/InternalRolez.g:60:9: 'start'
            {
            match("start"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__63"

    // $ANTLR start "T__64"
    public final void mT__64() throws RecognitionException {
        try {
            int _type = T__64;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // ch.trick17.rolez.lang.ui/ch.trick17.rolez.lang/target/xtext-gen/ch/trick17/rolez/lang/ui/contentassist/antlr/internal/InternalRolez.g:61:7: ( 'null' )
            // ch.trick17.rolez.lang.ui/ch.trick17.rolez.lang/target/xtext-gen/ch/trick17/rolez/lang/ui/contentassist/antlr/internal/InternalRolez.g:61:9: 'null'
            {
            match("null"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__64"

    // $ANTLR start "T__65"
    public final void mT__65() throws RecognitionException {
        try {
            int _type = T__65;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // ch.trick17.rolez.lang.ui/ch.trick17.rolez.lang/target/xtext-gen/ch/trick17/rolez/lang/ui/contentassist/antlr/internal/InternalRolez.g:62:7: ( 'int' )
            // ch.trick17.rolez.lang.ui/ch.trick17.rolez.lang/target/xtext-gen/ch/trick17/rolez/lang/ui/contentassist/antlr/internal/InternalRolez.g:62:9: 'int'
            {
            match("int"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__65"

    // $ANTLR start "T__66"
    public final void mT__66() throws RecognitionException {
        try {
            int _type = T__66;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // ch.trick17.rolez.lang.ui/ch.trick17.rolez.lang/target/xtext-gen/ch/trick17/rolez/lang/ui/contentassist/antlr/internal/InternalRolez.g:63:7: ( 'double' )
            // ch.trick17.rolez.lang.ui/ch.trick17.rolez.lang/target/xtext-gen/ch/trick17/rolez/lang/ui/contentassist/antlr/internal/InternalRolez.g:63:9: 'double'
            {
            match("double"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__66"

    // $ANTLR start "T__67"
    public final void mT__67() throws RecognitionException {
        try {
            int _type = T__67;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // ch.trick17.rolez.lang.ui/ch.trick17.rolez.lang/target/xtext-gen/ch/trick17/rolez/lang/ui/contentassist/antlr/internal/InternalRolez.g:64:7: ( 'boolean' )
            // ch.trick17.rolez.lang.ui/ch.trick17.rolez.lang/target/xtext-gen/ch/trick17/rolez/lang/ui/contentassist/antlr/internal/InternalRolez.g:64:9: 'boolean'
            {
            match("boolean"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__67"

    // $ANTLR start "T__68"
    public final void mT__68() throws RecognitionException {
        try {
            int _type = T__68;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // ch.trick17.rolez.lang.ui/ch.trick17.rolez.lang/target/xtext-gen/ch/trick17/rolez/lang/ui/contentassist/antlr/internal/InternalRolez.g:65:7: ( 'char' )
            // ch.trick17.rolez.lang.ui/ch.trick17.rolez.lang/target/xtext-gen/ch/trick17/rolez/lang/ui/contentassist/antlr/internal/InternalRolez.g:65:9: 'char'
            {
            match("char"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__68"

    // $ANTLR start "T__69"
    public final void mT__69() throws RecognitionException {
        try {
            int _type = T__69;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // ch.trick17.rolez.lang.ui/ch.trick17.rolez.lang/target/xtext-gen/ch/trick17/rolez/lang/ui/contentassist/antlr/internal/InternalRolez.g:66:7: ( 'void' )
            // ch.trick17.rolez.lang.ui/ch.trick17.rolez.lang/target/xtext-gen/ch/trick17/rolez/lang/ui/contentassist/antlr/internal/InternalRolez.g:66:9: 'void'
            {
            match("void"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__69"

    // $ANTLR start "T__70"
    public final void mT__70() throws RecognitionException {
        try {
            int _type = T__70;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // ch.trick17.rolez.lang.ui/ch.trick17.rolez.lang/target/xtext-gen/ch/trick17/rolez/lang/ui/contentassist/antlr/internal/InternalRolez.g:67:7: ( 'mapped' )
            // ch.trick17.rolez.lang.ui/ch.trick17.rolez.lang/target/xtext-gen/ch/trick17/rolez/lang/ui/contentassist/antlr/internal/InternalRolez.g:67:9: 'mapped'
            {
            match("mapped"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__70"

    // $ANTLR start "T__71"
    public final void mT__71() throws RecognitionException {
        try {
            int _type = T__71;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // ch.trick17.rolez.lang.ui/ch.trick17.rolez.lang/target/xtext-gen/ch/trick17/rolez/lang/ui/contentassist/antlr/internal/InternalRolez.g:68:7: ( 'override' )
            // ch.trick17.rolez.lang.ui/ch.trick17.rolez.lang/target/xtext-gen/ch/trick17/rolez/lang/ui/contentassist/antlr/internal/InternalRolez.g:68:9: 'override'
            {
            match("override"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__71"

    // $ANTLR start "RULE_STRING"
    public final void mRULE_STRING() throws RecognitionException {
        try {
            int _type = RULE_STRING;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // ch.trick17.rolez.lang.ui/ch.trick17.rolez.lang/target/xtext-gen/ch/trick17/rolez/lang/ui/contentassist/antlr/internal/InternalRolez.g:13160:13: ( '\"' ( '\\\\' ( 'b' | 't' | 'n' | 'f' | 'r' | 'u' | '\"' | '\\'' | '\\\\' ) | ~ ( ( '\\\\' | '\"' ) ) )* '\"' )
            // ch.trick17.rolez.lang.ui/ch.trick17.rolez.lang/target/xtext-gen/ch/trick17/rolez/lang/ui/contentassist/antlr/internal/InternalRolez.g:13160:15: '\"' ( '\\\\' ( 'b' | 't' | 'n' | 'f' | 'r' | 'u' | '\"' | '\\'' | '\\\\' ) | ~ ( ( '\\\\' | '\"' ) ) )* '\"'
            {
            match('\"'); 
            // ch.trick17.rolez.lang.ui/ch.trick17.rolez.lang/target/xtext-gen/ch/trick17/rolez/lang/ui/contentassist/antlr/internal/InternalRolez.g:13160:19: ( '\\\\' ( 'b' | 't' | 'n' | 'f' | 'r' | 'u' | '\"' | '\\'' | '\\\\' ) | ~ ( ( '\\\\' | '\"' ) ) )*
            loop1:
            do {
                int alt1=3;
                int LA1_0 = input.LA(1);

                if ( (LA1_0=='\\') ) {
                    alt1=1;
                }
                else if ( ((LA1_0>='\u0000' && LA1_0<='!')||(LA1_0>='#' && LA1_0<='[')||(LA1_0>=']' && LA1_0<='\uFFFF')) ) {
                    alt1=2;
                }


                switch (alt1) {
            	case 1 :
            	    // ch.trick17.rolez.lang.ui/ch.trick17.rolez.lang/target/xtext-gen/ch/trick17/rolez/lang/ui/contentassist/antlr/internal/InternalRolez.g:13160:20: '\\\\' ( 'b' | 't' | 'n' | 'f' | 'r' | 'u' | '\"' | '\\'' | '\\\\' )
            	    {
            	    match('\\'); 
            	    if ( input.LA(1)=='\"'||input.LA(1)=='\''||input.LA(1)=='\\'||input.LA(1)=='b'||input.LA(1)=='f'||input.LA(1)=='n'||input.LA(1)=='r'||(input.LA(1)>='t' && input.LA(1)<='u') ) {
            	        input.consume();

            	    }
            	    else {
            	        MismatchedSetException mse = new MismatchedSetException(null,input);
            	        recover(mse);
            	        throw mse;}


            	    }
            	    break;
            	case 2 :
            	    // ch.trick17.rolez.lang.ui/ch.trick17.rolez.lang/target/xtext-gen/ch/trick17/rolez/lang/ui/contentassist/antlr/internal/InternalRolez.g:13160:65: ~ ( ( '\\\\' | '\"' ) )
            	    {
            	    if ( (input.LA(1)>='\u0000' && input.LA(1)<='!')||(input.LA(1)>='#' && input.LA(1)<='[')||(input.LA(1)>=']' && input.LA(1)<='\uFFFF') ) {
            	        input.consume();

            	    }
            	    else {
            	        MismatchedSetException mse = new MismatchedSetException(null,input);
            	        recover(mse);
            	        throw mse;}


            	    }
            	    break;

            	default :
            	    break loop1;
                }
            } while (true);

            match('\"'); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "RULE_STRING"

    // $ANTLR start "RULE_DOUBLE"
    public final void mRULE_DOUBLE() throws RecognitionException {
        try {
            int _type = RULE_DOUBLE;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // ch.trick17.rolez.lang.ui/ch.trick17.rolez.lang/target/xtext-gen/ch/trick17/rolez/lang/ui/contentassist/antlr/internal/InternalRolez.g:13162:13: ( RULE_INT '.' RULE_INT )
            // ch.trick17.rolez.lang.ui/ch.trick17.rolez.lang/target/xtext-gen/ch/trick17/rolez/lang/ui/contentassist/antlr/internal/InternalRolez.g:13162:15: RULE_INT '.' RULE_INT
            {
            mRULE_INT(); 
            match('.'); 
            mRULE_INT(); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "RULE_DOUBLE"

    // $ANTLR start "RULE_CHAR"
    public final void mRULE_CHAR() throws RecognitionException {
        try {
            int _type = RULE_CHAR;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // ch.trick17.rolez.lang.ui/ch.trick17.rolez.lang/target/xtext-gen/ch/trick17/rolez/lang/ui/contentassist/antlr/internal/InternalRolez.g:13164:11: ( '\\'' ( '\\\\' ( 'b' | 't' | 'n' | 'f' | 'r' | 'u' | '\"' | '\\'' | '\\\\' ) | ~ ( ( '\\\\' | '\\'' ) ) ) '\\'' )
            // ch.trick17.rolez.lang.ui/ch.trick17.rolez.lang/target/xtext-gen/ch/trick17/rolez/lang/ui/contentassist/antlr/internal/InternalRolez.g:13164:13: '\\'' ( '\\\\' ( 'b' | 't' | 'n' | 'f' | 'r' | 'u' | '\"' | '\\'' | '\\\\' ) | ~ ( ( '\\\\' | '\\'' ) ) ) '\\''
            {
            match('\''); 
            // ch.trick17.rolez.lang.ui/ch.trick17.rolez.lang/target/xtext-gen/ch/trick17/rolez/lang/ui/contentassist/antlr/internal/InternalRolez.g:13164:18: ( '\\\\' ( 'b' | 't' | 'n' | 'f' | 'r' | 'u' | '\"' | '\\'' | '\\\\' ) | ~ ( ( '\\\\' | '\\'' ) ) )
            int alt2=2;
            int LA2_0 = input.LA(1);

            if ( (LA2_0=='\\') ) {
                alt2=1;
            }
            else if ( ((LA2_0>='\u0000' && LA2_0<='&')||(LA2_0>='(' && LA2_0<='[')||(LA2_0>=']' && LA2_0<='\uFFFF')) ) {
                alt2=2;
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("", 2, 0, input);

                throw nvae;
            }
            switch (alt2) {
                case 1 :
                    // ch.trick17.rolez.lang.ui/ch.trick17.rolez.lang/target/xtext-gen/ch/trick17/rolez/lang/ui/contentassist/antlr/internal/InternalRolez.g:13164:19: '\\\\' ( 'b' | 't' | 'n' | 'f' | 'r' | 'u' | '\"' | '\\'' | '\\\\' )
                    {
                    match('\\'); 
                    if ( input.LA(1)=='\"'||input.LA(1)=='\''||input.LA(1)=='\\'||input.LA(1)=='b'||input.LA(1)=='f'||input.LA(1)=='n'||input.LA(1)=='r'||(input.LA(1)>='t' && input.LA(1)<='u') ) {
                        input.consume();

                    }
                    else {
                        MismatchedSetException mse = new MismatchedSetException(null,input);
                        recover(mse);
                        throw mse;}


                    }
                    break;
                case 2 :
                    // ch.trick17.rolez.lang.ui/ch.trick17.rolez.lang/target/xtext-gen/ch/trick17/rolez/lang/ui/contentassist/antlr/internal/InternalRolez.g:13164:64: ~ ( ( '\\\\' | '\\'' ) )
                    {
                    if ( (input.LA(1)>='\u0000' && input.LA(1)<='&')||(input.LA(1)>='(' && input.LA(1)<='[')||(input.LA(1)>=']' && input.LA(1)<='\uFFFF') ) {
                        input.consume();

                    }
                    else {
                        MismatchedSetException mse = new MismatchedSetException(null,input);
                        recover(mse);
                        throw mse;}


                    }
                    break;

            }

            match('\''); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "RULE_CHAR"

    // $ANTLR start "RULE_NULL_TYPE"
    public final void mRULE_NULL_TYPE() throws RecognitionException {
        try {
            int _type = RULE_NULL_TYPE;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // ch.trick17.rolez.lang.ui/ch.trick17.rolez.lang/target/xtext-gen/ch/trick17/rolez/lang/ui/contentassist/antlr/internal/InternalRolez.g:13166:16: ( 'Null' )
            // ch.trick17.rolez.lang.ui/ch.trick17.rolez.lang/target/xtext-gen/ch/trick17/rolez/lang/ui/contentassist/antlr/internal/InternalRolez.g:13166:18: 'Null'
            {
            match("Null"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "RULE_NULL_TYPE"

    // $ANTLR start "RULE_ID"
    public final void mRULE_ID() throws RecognitionException {
        try {
            int _type = RULE_ID;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // ch.trick17.rolez.lang.ui/ch.trick17.rolez.lang/target/xtext-gen/ch/trick17/rolez/lang/ui/contentassist/antlr/internal/InternalRolez.g:13168:9: ( ( '^' )? ( 'a' .. 'z' | 'A' .. 'Z' | '_' ) ( 'a' .. 'z' | 'A' .. 'Z' | '_' | '0' .. '9' )* )
            // ch.trick17.rolez.lang.ui/ch.trick17.rolez.lang/target/xtext-gen/ch/trick17/rolez/lang/ui/contentassist/antlr/internal/InternalRolez.g:13168:11: ( '^' )? ( 'a' .. 'z' | 'A' .. 'Z' | '_' ) ( 'a' .. 'z' | 'A' .. 'Z' | '_' | '0' .. '9' )*
            {
            // ch.trick17.rolez.lang.ui/ch.trick17.rolez.lang/target/xtext-gen/ch/trick17/rolez/lang/ui/contentassist/antlr/internal/InternalRolez.g:13168:11: ( '^' )?
            int alt3=2;
            int LA3_0 = input.LA(1);

            if ( (LA3_0=='^') ) {
                alt3=1;
            }
            switch (alt3) {
                case 1 :
                    // ch.trick17.rolez.lang.ui/ch.trick17.rolez.lang/target/xtext-gen/ch/trick17/rolez/lang/ui/contentassist/antlr/internal/InternalRolez.g:13168:11: '^'
                    {
                    match('^'); 

                    }
                    break;

            }

            if ( (input.LA(1)>='A' && input.LA(1)<='Z')||input.LA(1)=='_'||(input.LA(1)>='a' && input.LA(1)<='z') ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            // ch.trick17.rolez.lang.ui/ch.trick17.rolez.lang/target/xtext-gen/ch/trick17/rolez/lang/ui/contentassist/antlr/internal/InternalRolez.g:13168:40: ( 'a' .. 'z' | 'A' .. 'Z' | '_' | '0' .. '9' )*
            loop4:
            do {
                int alt4=2;
                int LA4_0 = input.LA(1);

                if ( ((LA4_0>='0' && LA4_0<='9')||(LA4_0>='A' && LA4_0<='Z')||LA4_0=='_'||(LA4_0>='a' && LA4_0<='z')) ) {
                    alt4=1;
                }


                switch (alt4) {
            	case 1 :
            	    // ch.trick17.rolez.lang.ui/ch.trick17.rolez.lang/target/xtext-gen/ch/trick17/rolez/lang/ui/contentassist/antlr/internal/InternalRolez.g:
            	    {
            	    if ( (input.LA(1)>='0' && input.LA(1)<='9')||(input.LA(1)>='A' && input.LA(1)<='Z')||input.LA(1)=='_'||(input.LA(1)>='a' && input.LA(1)<='z') ) {
            	        input.consume();

            	    }
            	    else {
            	        MismatchedSetException mse = new MismatchedSetException(null,input);
            	        recover(mse);
            	        throw mse;}


            	    }
            	    break;

            	default :
            	    break loop4;
                }
            } while (true);


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "RULE_ID"

    // $ANTLR start "RULE_INT"
    public final void mRULE_INT() throws RecognitionException {
        try {
            int _type = RULE_INT;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // ch.trick17.rolez.lang.ui/ch.trick17.rolez.lang/target/xtext-gen/ch/trick17/rolez/lang/ui/contentassist/antlr/internal/InternalRolez.g:13170:10: ( ( '0' .. '9' )+ )
            // ch.trick17.rolez.lang.ui/ch.trick17.rolez.lang/target/xtext-gen/ch/trick17/rolez/lang/ui/contentassist/antlr/internal/InternalRolez.g:13170:12: ( '0' .. '9' )+
            {
            // ch.trick17.rolez.lang.ui/ch.trick17.rolez.lang/target/xtext-gen/ch/trick17/rolez/lang/ui/contentassist/antlr/internal/InternalRolez.g:13170:12: ( '0' .. '9' )+
            int cnt5=0;
            loop5:
            do {
                int alt5=2;
                int LA5_0 = input.LA(1);

                if ( ((LA5_0>='0' && LA5_0<='9')) ) {
                    alt5=1;
                }


                switch (alt5) {
            	case 1 :
            	    // ch.trick17.rolez.lang.ui/ch.trick17.rolez.lang/target/xtext-gen/ch/trick17/rolez/lang/ui/contentassist/antlr/internal/InternalRolez.g:13170:13: '0' .. '9'
            	    {
            	    matchRange('0','9'); 

            	    }
            	    break;

            	default :
            	    if ( cnt5 >= 1 ) break loop5;
                        EarlyExitException eee =
                            new EarlyExitException(5, input);
                        throw eee;
                }
                cnt5++;
            } while (true);


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "RULE_INT"

    // $ANTLR start "RULE_ML_COMMENT"
    public final void mRULE_ML_COMMENT() throws RecognitionException {
        try {
            int _type = RULE_ML_COMMENT;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // ch.trick17.rolez.lang.ui/ch.trick17.rolez.lang/target/xtext-gen/ch/trick17/rolez/lang/ui/contentassist/antlr/internal/InternalRolez.g:13172:17: ( '/*' ( options {greedy=false; } : . )* '*/' )
            // ch.trick17.rolez.lang.ui/ch.trick17.rolez.lang/target/xtext-gen/ch/trick17/rolez/lang/ui/contentassist/antlr/internal/InternalRolez.g:13172:19: '/*' ( options {greedy=false; } : . )* '*/'
            {
            match("/*"); 

            // ch.trick17.rolez.lang.ui/ch.trick17.rolez.lang/target/xtext-gen/ch/trick17/rolez/lang/ui/contentassist/antlr/internal/InternalRolez.g:13172:24: ( options {greedy=false; } : . )*
            loop6:
            do {
                int alt6=2;
                int LA6_0 = input.LA(1);

                if ( (LA6_0=='*') ) {
                    int LA6_1 = input.LA(2);

                    if ( (LA6_1=='/') ) {
                        alt6=2;
                    }
                    else if ( ((LA6_1>='\u0000' && LA6_1<='.')||(LA6_1>='0' && LA6_1<='\uFFFF')) ) {
                        alt6=1;
                    }


                }
                else if ( ((LA6_0>='\u0000' && LA6_0<=')')||(LA6_0>='+' && LA6_0<='\uFFFF')) ) {
                    alt6=1;
                }


                switch (alt6) {
            	case 1 :
            	    // ch.trick17.rolez.lang.ui/ch.trick17.rolez.lang/target/xtext-gen/ch/trick17/rolez/lang/ui/contentassist/antlr/internal/InternalRolez.g:13172:52: .
            	    {
            	    matchAny(); 

            	    }
            	    break;

            	default :
            	    break loop6;
                }
            } while (true);

            match("*/"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "RULE_ML_COMMENT"

    // $ANTLR start "RULE_SL_COMMENT"
    public final void mRULE_SL_COMMENT() throws RecognitionException {
        try {
            int _type = RULE_SL_COMMENT;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // ch.trick17.rolez.lang.ui/ch.trick17.rolez.lang/target/xtext-gen/ch/trick17/rolez/lang/ui/contentassist/antlr/internal/InternalRolez.g:13174:17: ( '//' (~ ( ( '\\n' | '\\r' ) ) )* ( ( '\\r' )? '\\n' )? )
            // ch.trick17.rolez.lang.ui/ch.trick17.rolez.lang/target/xtext-gen/ch/trick17/rolez/lang/ui/contentassist/antlr/internal/InternalRolez.g:13174:19: '//' (~ ( ( '\\n' | '\\r' ) ) )* ( ( '\\r' )? '\\n' )?
            {
            match("//"); 

            // ch.trick17.rolez.lang.ui/ch.trick17.rolez.lang/target/xtext-gen/ch/trick17/rolez/lang/ui/contentassist/antlr/internal/InternalRolez.g:13174:24: (~ ( ( '\\n' | '\\r' ) ) )*
            loop7:
            do {
                int alt7=2;
                int LA7_0 = input.LA(1);

                if ( ((LA7_0>='\u0000' && LA7_0<='\t')||(LA7_0>='\u000B' && LA7_0<='\f')||(LA7_0>='\u000E' && LA7_0<='\uFFFF')) ) {
                    alt7=1;
                }


                switch (alt7) {
            	case 1 :
            	    // ch.trick17.rolez.lang.ui/ch.trick17.rolez.lang/target/xtext-gen/ch/trick17/rolez/lang/ui/contentassist/antlr/internal/InternalRolez.g:13174:24: ~ ( ( '\\n' | '\\r' ) )
            	    {
            	    if ( (input.LA(1)>='\u0000' && input.LA(1)<='\t')||(input.LA(1)>='\u000B' && input.LA(1)<='\f')||(input.LA(1)>='\u000E' && input.LA(1)<='\uFFFF') ) {
            	        input.consume();

            	    }
            	    else {
            	        MismatchedSetException mse = new MismatchedSetException(null,input);
            	        recover(mse);
            	        throw mse;}


            	    }
            	    break;

            	default :
            	    break loop7;
                }
            } while (true);

            // ch.trick17.rolez.lang.ui/ch.trick17.rolez.lang/target/xtext-gen/ch/trick17/rolez/lang/ui/contentassist/antlr/internal/InternalRolez.g:13174:40: ( ( '\\r' )? '\\n' )?
            int alt9=2;
            int LA9_0 = input.LA(1);

            if ( (LA9_0=='\n'||LA9_0=='\r') ) {
                alt9=1;
            }
            switch (alt9) {
                case 1 :
                    // ch.trick17.rolez.lang.ui/ch.trick17.rolez.lang/target/xtext-gen/ch/trick17/rolez/lang/ui/contentassist/antlr/internal/InternalRolez.g:13174:41: ( '\\r' )? '\\n'
                    {
                    // ch.trick17.rolez.lang.ui/ch.trick17.rolez.lang/target/xtext-gen/ch/trick17/rolez/lang/ui/contentassist/antlr/internal/InternalRolez.g:13174:41: ( '\\r' )?
                    int alt8=2;
                    int LA8_0 = input.LA(1);

                    if ( (LA8_0=='\r') ) {
                        alt8=1;
                    }
                    switch (alt8) {
                        case 1 :
                            // ch.trick17.rolez.lang.ui/ch.trick17.rolez.lang/target/xtext-gen/ch/trick17/rolez/lang/ui/contentassist/antlr/internal/InternalRolez.g:13174:41: '\\r'
                            {
                            match('\r'); 

                            }
                            break;

                    }

                    match('\n'); 

                    }
                    break;

            }


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "RULE_SL_COMMENT"

    // $ANTLR start "RULE_WS"
    public final void mRULE_WS() throws RecognitionException {
        try {
            int _type = RULE_WS;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // ch.trick17.rolez.lang.ui/ch.trick17.rolez.lang/target/xtext-gen/ch/trick17/rolez/lang/ui/contentassist/antlr/internal/InternalRolez.g:13176:9: ( ( ' ' | '\\t' | '\\r' | '\\n' )+ )
            // ch.trick17.rolez.lang.ui/ch.trick17.rolez.lang/target/xtext-gen/ch/trick17/rolez/lang/ui/contentassist/antlr/internal/InternalRolez.g:13176:11: ( ' ' | '\\t' | '\\r' | '\\n' )+
            {
            // ch.trick17.rolez.lang.ui/ch.trick17.rolez.lang/target/xtext-gen/ch/trick17/rolez/lang/ui/contentassist/antlr/internal/InternalRolez.g:13176:11: ( ' ' | '\\t' | '\\r' | '\\n' )+
            int cnt10=0;
            loop10:
            do {
                int alt10=2;
                int LA10_0 = input.LA(1);

                if ( ((LA10_0>='\t' && LA10_0<='\n')||LA10_0=='\r'||LA10_0==' ') ) {
                    alt10=1;
                }


                switch (alt10) {
            	case 1 :
            	    // ch.trick17.rolez.lang.ui/ch.trick17.rolez.lang/target/xtext-gen/ch/trick17/rolez/lang/ui/contentassist/antlr/internal/InternalRolez.g:
            	    {
            	    if ( (input.LA(1)>='\t' && input.LA(1)<='\n')||input.LA(1)=='\r'||input.LA(1)==' ' ) {
            	        input.consume();

            	    }
            	    else {
            	        MismatchedSetException mse = new MismatchedSetException(null,input);
            	        recover(mse);
            	        throw mse;}


            	    }
            	    break;

            	default :
            	    if ( cnt10 >= 1 ) break loop10;
                        EarlyExitException eee =
                            new EarlyExitException(10, input);
                        throw eee;
                }
                cnt10++;
            } while (true);


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "RULE_WS"

    // $ANTLR start "RULE_ANY_OTHER"
    public final void mRULE_ANY_OTHER() throws RecognitionException {
        try {
            int _type = RULE_ANY_OTHER;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // ch.trick17.rolez.lang.ui/ch.trick17.rolez.lang/target/xtext-gen/ch/trick17/rolez/lang/ui/contentassist/antlr/internal/InternalRolez.g:13178:16: ( . )
            // ch.trick17.rolez.lang.ui/ch.trick17.rolez.lang/target/xtext-gen/ch/trick17/rolez/lang/ui/contentassist/antlr/internal/InternalRolez.g:13178:18: .
            {
            matchAny(); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "RULE_ANY_OTHER"

    public void mTokens() throws RecognitionException {
        // ch.trick17.rolez.lang.ui/ch.trick17.rolez.lang/target/xtext-gen/ch/trick17/rolez/lang/ui/contentassist/antlr/internal/InternalRolez.g:1:8: ( T__14 | T__15 | T__16 | T__17 | T__18 | T__19 | T__20 | T__21 | T__22 | T__23 | T__24 | T__25 | T__26 | T__27 | T__28 | T__29 | T__30 | T__31 | T__32 | T__33 | T__34 | T__35 | T__36 | T__37 | T__38 | T__39 | T__40 | T__41 | T__42 | T__43 | T__44 | T__45 | T__46 | T__47 | T__48 | T__49 | T__50 | T__51 | T__52 | T__53 | T__54 | T__55 | T__56 | T__57 | T__58 | T__59 | T__60 | T__61 | T__62 | T__63 | T__64 | T__65 | T__66 | T__67 | T__68 | T__69 | T__70 | T__71 | RULE_STRING | RULE_DOUBLE | RULE_CHAR | RULE_NULL_TYPE | RULE_ID | RULE_INT | RULE_ML_COMMENT | RULE_SL_COMMENT | RULE_WS | RULE_ANY_OTHER )
        int alt11=68;
        alt11 = dfa11.predict(input);
        switch (alt11) {
            case 1 :
                // ch.trick17.rolez.lang.ui/ch.trick17.rolez.lang/target/xtext-gen/ch/trick17/rolez/lang/ui/contentassist/antlr/internal/InternalRolez.g:1:10: T__14
                {
                mT__14(); 

                }
                break;
            case 2 :
                // ch.trick17.rolez.lang.ui/ch.trick17.rolez.lang/target/xtext-gen/ch/trick17/rolez/lang/ui/contentassist/antlr/internal/InternalRolez.g:1:16: T__15
                {
                mT__15(); 

                }
                break;
            case 3 :
                // ch.trick17.rolez.lang.ui/ch.trick17.rolez.lang/target/xtext-gen/ch/trick17/rolez/lang/ui/contentassist/antlr/internal/InternalRolez.g:1:22: T__16
                {
                mT__16(); 

                }
                break;
            case 4 :
                // ch.trick17.rolez.lang.ui/ch.trick17.rolez.lang/target/xtext-gen/ch/trick17/rolez/lang/ui/contentassist/antlr/internal/InternalRolez.g:1:28: T__17
                {
                mT__17(); 

                }
                break;
            case 5 :
                // ch.trick17.rolez.lang.ui/ch.trick17.rolez.lang/target/xtext-gen/ch/trick17/rolez/lang/ui/contentassist/antlr/internal/InternalRolez.g:1:34: T__18
                {
                mT__18(); 

                }
                break;
            case 6 :
                // ch.trick17.rolez.lang.ui/ch.trick17.rolez.lang/target/xtext-gen/ch/trick17/rolez/lang/ui/contentassist/antlr/internal/InternalRolez.g:1:40: T__19
                {
                mT__19(); 

                }
                break;
            case 7 :
                // ch.trick17.rolez.lang.ui/ch.trick17.rolez.lang/target/xtext-gen/ch/trick17/rolez/lang/ui/contentassist/antlr/internal/InternalRolez.g:1:46: T__20
                {
                mT__20(); 

                }
                break;
            case 8 :
                // ch.trick17.rolez.lang.ui/ch.trick17.rolez.lang/target/xtext-gen/ch/trick17/rolez/lang/ui/contentassist/antlr/internal/InternalRolez.g:1:52: T__21
                {
                mT__21(); 

                }
                break;
            case 9 :
                // ch.trick17.rolez.lang.ui/ch.trick17.rolez.lang/target/xtext-gen/ch/trick17/rolez/lang/ui/contentassist/antlr/internal/InternalRolez.g:1:58: T__22
                {
                mT__22(); 

                }
                break;
            case 10 :
                // ch.trick17.rolez.lang.ui/ch.trick17.rolez.lang/target/xtext-gen/ch/trick17/rolez/lang/ui/contentassist/antlr/internal/InternalRolez.g:1:64: T__23
                {
                mT__23(); 

                }
                break;
            case 11 :
                // ch.trick17.rolez.lang.ui/ch.trick17.rolez.lang/target/xtext-gen/ch/trick17/rolez/lang/ui/contentassist/antlr/internal/InternalRolez.g:1:70: T__24
                {
                mT__24(); 

                }
                break;
            case 12 :
                // ch.trick17.rolez.lang.ui/ch.trick17.rolez.lang/target/xtext-gen/ch/trick17/rolez/lang/ui/contentassist/antlr/internal/InternalRolez.g:1:76: T__25
                {
                mT__25(); 

                }
                break;
            case 13 :
                // ch.trick17.rolez.lang.ui/ch.trick17.rolez.lang/target/xtext-gen/ch/trick17/rolez/lang/ui/contentassist/antlr/internal/InternalRolez.g:1:82: T__26
                {
                mT__26(); 

                }
                break;
            case 14 :
                // ch.trick17.rolez.lang.ui/ch.trick17.rolez.lang/target/xtext-gen/ch/trick17/rolez/lang/ui/contentassist/antlr/internal/InternalRolez.g:1:88: T__27
                {
                mT__27(); 

                }
                break;
            case 15 :
                // ch.trick17.rolez.lang.ui/ch.trick17.rolez.lang/target/xtext-gen/ch/trick17/rolez/lang/ui/contentassist/antlr/internal/InternalRolez.g:1:94: T__28
                {
                mT__28(); 

                }
                break;
            case 16 :
                // ch.trick17.rolez.lang.ui/ch.trick17.rolez.lang/target/xtext-gen/ch/trick17/rolez/lang/ui/contentassist/antlr/internal/InternalRolez.g:1:100: T__29
                {
                mT__29(); 

                }
                break;
            case 17 :
                // ch.trick17.rolez.lang.ui/ch.trick17.rolez.lang/target/xtext-gen/ch/trick17/rolez/lang/ui/contentassist/antlr/internal/InternalRolez.g:1:106: T__30
                {
                mT__30(); 

                }
                break;
            case 18 :
                // ch.trick17.rolez.lang.ui/ch.trick17.rolez.lang/target/xtext-gen/ch/trick17/rolez/lang/ui/contentassist/antlr/internal/InternalRolez.g:1:112: T__31
                {
                mT__31(); 

                }
                break;
            case 19 :
                // ch.trick17.rolez.lang.ui/ch.trick17.rolez.lang/target/xtext-gen/ch/trick17/rolez/lang/ui/contentassist/antlr/internal/InternalRolez.g:1:118: T__32
                {
                mT__32(); 

                }
                break;
            case 20 :
                // ch.trick17.rolez.lang.ui/ch.trick17.rolez.lang/target/xtext-gen/ch/trick17/rolez/lang/ui/contentassist/antlr/internal/InternalRolez.g:1:124: T__33
                {
                mT__33(); 

                }
                break;
            case 21 :
                // ch.trick17.rolez.lang.ui/ch.trick17.rolez.lang/target/xtext-gen/ch/trick17/rolez/lang/ui/contentassist/antlr/internal/InternalRolez.g:1:130: T__34
                {
                mT__34(); 

                }
                break;
            case 22 :
                // ch.trick17.rolez.lang.ui/ch.trick17.rolez.lang/target/xtext-gen/ch/trick17/rolez/lang/ui/contentassist/antlr/internal/InternalRolez.g:1:136: T__35
                {
                mT__35(); 

                }
                break;
            case 23 :
                // ch.trick17.rolez.lang.ui/ch.trick17.rolez.lang/target/xtext-gen/ch/trick17/rolez/lang/ui/contentassist/antlr/internal/InternalRolez.g:1:142: T__36
                {
                mT__36(); 

                }
                break;
            case 24 :
                // ch.trick17.rolez.lang.ui/ch.trick17.rolez.lang/target/xtext-gen/ch/trick17/rolez/lang/ui/contentassist/antlr/internal/InternalRolez.g:1:148: T__37
                {
                mT__37(); 

                }
                break;
            case 25 :
                // ch.trick17.rolez.lang.ui/ch.trick17.rolez.lang/target/xtext-gen/ch/trick17/rolez/lang/ui/contentassist/antlr/internal/InternalRolez.g:1:154: T__38
                {
                mT__38(); 

                }
                break;
            case 26 :
                // ch.trick17.rolez.lang.ui/ch.trick17.rolez.lang/target/xtext-gen/ch/trick17/rolez/lang/ui/contentassist/antlr/internal/InternalRolez.g:1:160: T__39
                {
                mT__39(); 

                }
                break;
            case 27 :
                // ch.trick17.rolez.lang.ui/ch.trick17.rolez.lang/target/xtext-gen/ch/trick17/rolez/lang/ui/contentassist/antlr/internal/InternalRolez.g:1:166: T__40
                {
                mT__40(); 

                }
                break;
            case 28 :
                // ch.trick17.rolez.lang.ui/ch.trick17.rolez.lang/target/xtext-gen/ch/trick17/rolez/lang/ui/contentassist/antlr/internal/InternalRolez.g:1:172: T__41
                {
                mT__41(); 

                }
                break;
            case 29 :
                // ch.trick17.rolez.lang.ui/ch.trick17.rolez.lang/target/xtext-gen/ch/trick17/rolez/lang/ui/contentassist/antlr/internal/InternalRolez.g:1:178: T__42
                {
                mT__42(); 

                }
                break;
            case 30 :
                // ch.trick17.rolez.lang.ui/ch.trick17.rolez.lang/target/xtext-gen/ch/trick17/rolez/lang/ui/contentassist/antlr/internal/InternalRolez.g:1:184: T__43
                {
                mT__43(); 

                }
                break;
            case 31 :
                // ch.trick17.rolez.lang.ui/ch.trick17.rolez.lang/target/xtext-gen/ch/trick17/rolez/lang/ui/contentassist/antlr/internal/InternalRolez.g:1:190: T__44
                {
                mT__44(); 

                }
                break;
            case 32 :
                // ch.trick17.rolez.lang.ui/ch.trick17.rolez.lang/target/xtext-gen/ch/trick17/rolez/lang/ui/contentassist/antlr/internal/InternalRolez.g:1:196: T__45
                {
                mT__45(); 

                }
                break;
            case 33 :
                // ch.trick17.rolez.lang.ui/ch.trick17.rolez.lang/target/xtext-gen/ch/trick17/rolez/lang/ui/contentassist/antlr/internal/InternalRolez.g:1:202: T__46
                {
                mT__46(); 

                }
                break;
            case 34 :
                // ch.trick17.rolez.lang.ui/ch.trick17.rolez.lang/target/xtext-gen/ch/trick17/rolez/lang/ui/contentassist/antlr/internal/InternalRolez.g:1:208: T__47
                {
                mT__47(); 

                }
                break;
            case 35 :
                // ch.trick17.rolez.lang.ui/ch.trick17.rolez.lang/target/xtext-gen/ch/trick17/rolez/lang/ui/contentassist/antlr/internal/InternalRolez.g:1:214: T__48
                {
                mT__48(); 

                }
                break;
            case 36 :
                // ch.trick17.rolez.lang.ui/ch.trick17.rolez.lang/target/xtext-gen/ch/trick17/rolez/lang/ui/contentassist/antlr/internal/InternalRolez.g:1:220: T__49
                {
                mT__49(); 

                }
                break;
            case 37 :
                // ch.trick17.rolez.lang.ui/ch.trick17.rolez.lang/target/xtext-gen/ch/trick17/rolez/lang/ui/contentassist/antlr/internal/InternalRolez.g:1:226: T__50
                {
                mT__50(); 

                }
                break;
            case 38 :
                // ch.trick17.rolez.lang.ui/ch.trick17.rolez.lang/target/xtext-gen/ch/trick17/rolez/lang/ui/contentassist/antlr/internal/InternalRolez.g:1:232: T__51
                {
                mT__51(); 

                }
                break;
            case 39 :
                // ch.trick17.rolez.lang.ui/ch.trick17.rolez.lang/target/xtext-gen/ch/trick17/rolez/lang/ui/contentassist/antlr/internal/InternalRolez.g:1:238: T__52
                {
                mT__52(); 

                }
                break;
            case 40 :
                // ch.trick17.rolez.lang.ui/ch.trick17.rolez.lang/target/xtext-gen/ch/trick17/rolez/lang/ui/contentassist/antlr/internal/InternalRolez.g:1:244: T__53
                {
                mT__53(); 

                }
                break;
            case 41 :
                // ch.trick17.rolez.lang.ui/ch.trick17.rolez.lang/target/xtext-gen/ch/trick17/rolez/lang/ui/contentassist/antlr/internal/InternalRolez.g:1:250: T__54
                {
                mT__54(); 

                }
                break;
            case 42 :
                // ch.trick17.rolez.lang.ui/ch.trick17.rolez.lang/target/xtext-gen/ch/trick17/rolez/lang/ui/contentassist/antlr/internal/InternalRolez.g:1:256: T__55
                {
                mT__55(); 

                }
                break;
            case 43 :
                // ch.trick17.rolez.lang.ui/ch.trick17.rolez.lang/target/xtext-gen/ch/trick17/rolez/lang/ui/contentassist/antlr/internal/InternalRolez.g:1:262: T__56
                {
                mT__56(); 

                }
                break;
            case 44 :
                // ch.trick17.rolez.lang.ui/ch.trick17.rolez.lang/target/xtext-gen/ch/trick17/rolez/lang/ui/contentassist/antlr/internal/InternalRolez.g:1:268: T__57
                {
                mT__57(); 

                }
                break;
            case 45 :
                // ch.trick17.rolez.lang.ui/ch.trick17.rolez.lang/target/xtext-gen/ch/trick17/rolez/lang/ui/contentassist/antlr/internal/InternalRolez.g:1:274: T__58
                {
                mT__58(); 

                }
                break;
            case 46 :
                // ch.trick17.rolez.lang.ui/ch.trick17.rolez.lang/target/xtext-gen/ch/trick17/rolez/lang/ui/contentassist/antlr/internal/InternalRolez.g:1:280: T__59
                {
                mT__59(); 

                }
                break;
            case 47 :
                // ch.trick17.rolez.lang.ui/ch.trick17.rolez.lang/target/xtext-gen/ch/trick17/rolez/lang/ui/contentassist/antlr/internal/InternalRolez.g:1:286: T__60
                {
                mT__60(); 

                }
                break;
            case 48 :
                // ch.trick17.rolez.lang.ui/ch.trick17.rolez.lang/target/xtext-gen/ch/trick17/rolez/lang/ui/contentassist/antlr/internal/InternalRolez.g:1:292: T__61
                {
                mT__61(); 

                }
                break;
            case 49 :
                // ch.trick17.rolez.lang.ui/ch.trick17.rolez.lang/target/xtext-gen/ch/trick17/rolez/lang/ui/contentassist/antlr/internal/InternalRolez.g:1:298: T__62
                {
                mT__62(); 

                }
                break;
            case 50 :
                // ch.trick17.rolez.lang.ui/ch.trick17.rolez.lang/target/xtext-gen/ch/trick17/rolez/lang/ui/contentassist/antlr/internal/InternalRolez.g:1:304: T__63
                {
                mT__63(); 

                }
                break;
            case 51 :
                // ch.trick17.rolez.lang.ui/ch.trick17.rolez.lang/target/xtext-gen/ch/trick17/rolez/lang/ui/contentassist/antlr/internal/InternalRolez.g:1:310: T__64
                {
                mT__64(); 

                }
                break;
            case 52 :
                // ch.trick17.rolez.lang.ui/ch.trick17.rolez.lang/target/xtext-gen/ch/trick17/rolez/lang/ui/contentassist/antlr/internal/InternalRolez.g:1:316: T__65
                {
                mT__65(); 

                }
                break;
            case 53 :
                // ch.trick17.rolez.lang.ui/ch.trick17.rolez.lang/target/xtext-gen/ch/trick17/rolez/lang/ui/contentassist/antlr/internal/InternalRolez.g:1:322: T__66
                {
                mT__66(); 

                }
                break;
            case 54 :
                // ch.trick17.rolez.lang.ui/ch.trick17.rolez.lang/target/xtext-gen/ch/trick17/rolez/lang/ui/contentassist/antlr/internal/InternalRolez.g:1:328: T__67
                {
                mT__67(); 

                }
                break;
            case 55 :
                // ch.trick17.rolez.lang.ui/ch.trick17.rolez.lang/target/xtext-gen/ch/trick17/rolez/lang/ui/contentassist/antlr/internal/InternalRolez.g:1:334: T__68
                {
                mT__68(); 

                }
                break;
            case 56 :
                // ch.trick17.rolez.lang.ui/ch.trick17.rolez.lang/target/xtext-gen/ch/trick17/rolez/lang/ui/contentassist/antlr/internal/InternalRolez.g:1:340: T__69
                {
                mT__69(); 

                }
                break;
            case 57 :
                // ch.trick17.rolez.lang.ui/ch.trick17.rolez.lang/target/xtext-gen/ch/trick17/rolez/lang/ui/contentassist/antlr/internal/InternalRolez.g:1:346: T__70
                {
                mT__70(); 

                }
                break;
            case 58 :
                // ch.trick17.rolez.lang.ui/ch.trick17.rolez.lang/target/xtext-gen/ch/trick17/rolez/lang/ui/contentassist/antlr/internal/InternalRolez.g:1:352: T__71
                {
                mT__71(); 

                }
                break;
            case 59 :
                // ch.trick17.rolez.lang.ui/ch.trick17.rolez.lang/target/xtext-gen/ch/trick17/rolez/lang/ui/contentassist/antlr/internal/InternalRolez.g:1:358: RULE_STRING
                {
                mRULE_STRING(); 

                }
                break;
            case 60 :
                // ch.trick17.rolez.lang.ui/ch.trick17.rolez.lang/target/xtext-gen/ch/trick17/rolez/lang/ui/contentassist/antlr/internal/InternalRolez.g:1:370: RULE_DOUBLE
                {
                mRULE_DOUBLE(); 

                }
                break;
            case 61 :
                // ch.trick17.rolez.lang.ui/ch.trick17.rolez.lang/target/xtext-gen/ch/trick17/rolez/lang/ui/contentassist/antlr/internal/InternalRolez.g:1:382: RULE_CHAR
                {
                mRULE_CHAR(); 

                }
                break;
            case 62 :
                // ch.trick17.rolez.lang.ui/ch.trick17.rolez.lang/target/xtext-gen/ch/trick17/rolez/lang/ui/contentassist/antlr/internal/InternalRolez.g:1:392: RULE_NULL_TYPE
                {
                mRULE_NULL_TYPE(); 

                }
                break;
            case 63 :
                // ch.trick17.rolez.lang.ui/ch.trick17.rolez.lang/target/xtext-gen/ch/trick17/rolez/lang/ui/contentassist/antlr/internal/InternalRolez.g:1:407: RULE_ID
                {
                mRULE_ID(); 

                }
                break;
            case 64 :
                // ch.trick17.rolez.lang.ui/ch.trick17.rolez.lang/target/xtext-gen/ch/trick17/rolez/lang/ui/contentassist/antlr/internal/InternalRolez.g:1:415: RULE_INT
                {
                mRULE_INT(); 

                }
                break;
            case 65 :
                // ch.trick17.rolez.lang.ui/ch.trick17.rolez.lang/target/xtext-gen/ch/trick17/rolez/lang/ui/contentassist/antlr/internal/InternalRolez.g:1:424: RULE_ML_COMMENT
                {
                mRULE_ML_COMMENT(); 

                }
                break;
            case 66 :
                // ch.trick17.rolez.lang.ui/ch.trick17.rolez.lang/target/xtext-gen/ch/trick17/rolez/lang/ui/contentassist/antlr/internal/InternalRolez.g:1:440: RULE_SL_COMMENT
                {
                mRULE_SL_COMMENT(); 

                }
                break;
            case 67 :
                // ch.trick17.rolez.lang.ui/ch.trick17.rolez.lang/target/xtext-gen/ch/trick17/rolez/lang/ui/contentassist/antlr/internal/InternalRolez.g:1:456: RULE_WS
                {
                mRULE_WS(); 

                }
                break;
            case 68 :
                // ch.trick17.rolez.lang.ui/ch.trick17.rolez.lang/target/xtext-gen/ch/trick17/rolez/lang/ui/contentassist/antlr/internal/InternalRolez.g:1:464: RULE_ANY_OTHER
                {
                mRULE_ANY_OTHER(); 

                }
                break;

        }

    }


    protected DFA11 dfa11 = new DFA11(this);
    static final String DFA11_eotS =
        "\1\uffff\1\60\2\55\3\60\1\72\1\74\1\76\1\100\3\uffff\1\106\1\uffff"+
        "\3\60\1\117\1\60\2\uffff\1\60\2\uffff\1\60\4\uffff\1\60\1\uffff"+
        "\5\60\1\55\1\150\1\55\1\60\1\55\3\uffff\2\60\3\uffff\6\60\17\uffff"+
        "\4\60\1\175\1\60\2\uffff\2\60\2\uffff\2\60\2\uffff\2\60\4\uffff"+
        "\2\60\1\uffff\3\60\1\u008a\2\60\3\uffff\1\150\1\uffff\1\60\1\uffff"+
        "\1\u008e\1\u008f\1\60\1\u0091\4\60\1\u0096\6\60\1\uffff\1\u009d"+
        "\6\60\1\u00a4\4\60\1\uffff\3\60\2\uffff\1\u00ac\1\uffff\1\60\1\u00ae"+
        "\1\u00af\1\u00b0\1\uffff\3\60\1\u00b5\2\60\1\uffff\1\60\1\u00b9"+
        "\1\60\1\u00bb\2\60\1\uffff\1\u00be\5\60\1\u00c4\1\uffff\1\60\3\uffff"+
        "\1\u00c6\3\60\1\uffff\2\60\1\u00cc\1\uffff\1\60\1\uffff\2\60\1\uffff"+
        "\1\u00d0\1\u00d1\1\u00d2\2\60\1\uffff\1\u00d5\1\uffff\2\60\1\u00d8"+
        "\1\60\1\u00da\1\uffff\1\60\1\u00dc\1\60\3\uffff\1\60\1\u00df\1\uffff"+
        "\2\60\1\uffff\1\u00e2\1\uffff\1\u00e3\1\uffff\1\60\1\u00e5\1\uffff"+
        "\1\60\1\u00e7\2\uffff\1\u00e8\1\uffff\1\u00e9\3\uffff";
    static final String DFA11_eofS =
        "\u00ea\uffff";
    static final String DFA11_minS =
        "\1\0\1\141\1\174\1\46\1\145\2\141\4\75\3\uffff\1\52\1\uffff\1\145"+
        "\1\141\1\146\1\52\1\150\2\uffff\1\154\2\uffff\1\142\4\uffff\1\145"+
        "\1\uffff\1\150\1\164\1\163\1\157\1\141\1\0\1\56\1\0\1\165\1\101"+
        "\3\uffff\1\154\1\151\3\uffff\1\146\2\165\1\163\1\145\1\154\17\uffff"+
        "\1\141\1\162\1\143\1\160\1\60\1\164\2\uffff\2\141\2\uffff\1\164"+
        "\1\163\2\uffff\1\152\1\145\4\uffff\1\167\1\154\1\uffff\1\151\1\160"+
        "\1\141\1\60\1\157\1\160\3\uffff\1\56\1\uffff\1\154\1\uffff\2\60"+
        "\1\144\1\60\1\142\1\145\1\153\1\163\1\60\1\163\1\144\1\165\1\145"+
        "\1\153\1\157\1\uffff\1\60\1\163\1\162\3\145\1\162\1\60\2\154\1\145"+
        "\1\162\1\uffff\1\154\1\160\1\154\2\uffff\1\60\1\uffff\1\154\3\60"+
        "\1\uffff\1\145\1\157\1\162\1\60\1\141\1\162\1\uffff\1\163\1\60\1"+
        "\156\1\60\1\143\1\162\1\uffff\1\60\1\145\1\162\1\164\2\145\1\60"+
        "\1\uffff\1\145\3\uffff\1\60\1\162\2\156\1\uffff\1\147\1\164\1\60"+
        "\1\uffff\1\144\1\uffff\1\164\1\151\1\uffff\3\60\1\141\1\144\1\uffff"+
        "\1\60\1\uffff\1\151\1\154\1\60\1\145\1\60\1\uffff\1\163\1\60\1\144"+
        "\3\uffff\1\156\1\60\1\uffff\1\164\1\171\1\uffff\1\60\1\uffff\1\60"+
        "\1\uffff\1\145\1\60\1\uffff\1\145\1\60\2\uffff\1\60\1\uffff\1\60"+
        "\3\uffff";
    static final String DFA11_maxS =
        "\1\uffff\1\157\1\174\1\46\1\157\1\162\1\141\4\75\3\uffff\1\57\1"+
        "\uffff\1\145\1\165\1\156\1\52\1\154\2\uffff\1\170\2\uffff\1\166"+
        "\4\uffff\1\165\1\uffff\1\150\1\165\1\163\1\157\1\141\1\uffff\1\71"+
        "\1\uffff\1\165\1\172\3\uffff\1\162\1\151\3\uffff\1\146\2\165\1\163"+
        "\1\151\1\154\17\uffff\1\164\1\162\1\143\1\160\1\172\1\164\2\uffff"+
        "\2\141\2\uffff\1\164\1\163\2\uffff\1\152\1\145\4\uffff\1\167\1\154"+
        "\1\uffff\1\151\1\160\1\141\1\172\1\157\1\160\3\uffff\1\71\1\uffff"+
        "\1\154\1\uffff\2\172\1\144\1\172\1\142\1\145\1\153\1\163\1\172\1"+
        "\163\1\144\1\165\1\145\1\153\1\157\1\uffff\1\172\1\163\1\162\3\145"+
        "\1\162\1\172\2\154\1\145\1\162\1\uffff\1\154\1\160\1\154\2\uffff"+
        "\1\172\1\uffff\1\154\3\172\1\uffff\1\145\1\167\1\162\1\172\1\141"+
        "\1\162\1\uffff\1\163\1\172\1\156\1\172\1\143\1\162\1\uffff\1\172"+
        "\1\145\1\162\1\164\2\145\1\172\1\uffff\1\145\3\uffff\1\172\1\162"+
        "\2\156\1\uffff\1\147\1\164\1\172\1\uffff\1\144\1\uffff\1\164\1\151"+
        "\1\uffff\3\172\1\141\1\144\1\uffff\1\172\1\uffff\1\151\1\154\1\172"+
        "\1\145\1\172\1\uffff\1\163\1\172\1\144\3\uffff\1\156\1\172\1\uffff"+
        "\1\164\1\171\1\uffff\1\172\1\uffff\1\172\1\uffff\1\145\1\172\1\uffff"+
        "\1\145\1\172\2\uffff\1\172\1\uffff\1\172\3\uffff";
    static final String DFA11_acceptS =
        "\13\uffff\1\16\1\17\1\20\1\uffff\1\22\5\uffff\1\33\1\34\1\uffff"+
        "\1\36\1\37\1\uffff\1\42\1\43\1\44\1\45\1\uffff\1\50\12\uffff\1\77"+
        "\1\103\1\104\2\uffff\1\77\1\2\1\3\6\uffff\1\10\1\47\1\11\1\57\1"+
        "\14\1\12\1\15\1\13\1\16\1\17\1\20\1\101\1\102\1\21\1\22\6\uffff"+
        "\1\31\1\30\2\uffff\1\33\1\34\2\uffff\1\36\1\37\2\uffff\1\42\1\43"+
        "\1\44\1\45\2\uffff\1\50\6\uffff\1\73\1\100\1\74\1\uffff\1\75\1\uffff"+
        "\1\103\17\uffff\1\51\14\uffff\1\56\3\uffff\1\1\1\7\1\uffff\1\4\4"+
        "\uffff\1\61\6\uffff\1\64\6\uffff\1\46\7\uffff\1\70\1\uffff\1\5\1"+
        "\41\1\60\4\uffff\1\25\3\uffff\1\67\1\uffff\1\52\2\uffff\1\63\5\uffff"+
        "\1\76\1\uffff\1\6\5\uffff\1\32\3\uffff\1\53\1\54\1\62\2\uffff\1"+
        "\65\2\uffff\1\55\1\uffff\1\27\1\uffff\1\40\2\uffff\1\71\2\uffff"+
        "\1\26\1\35\1\uffff\1\66\1\uffff\1\24\1\72\1\23";
    static final String DFA11_specialS =
        "\1\0\45\uffff\1\1\1\uffff\1\2\u00c1\uffff}>";
    static final String[] DFA11_transitionS = {
            "\11\55\2\54\2\55\1\54\22\55\1\54\1\10\1\46\2\55\1\17\1\3\1"+
            "\50\1\34\1\35\1\15\1\13\1\36\1\14\1\23\1\16\12\47\1\33\1\40"+
            "\1\11\1\7\1\12\2\55\15\53\1\51\14\53\1\25\1\55\1\26\1\52\1\53"+
            "\1\55\1\43\1\44\1\24\1\4\1\27\1\6\2\53\1\22\3\53\1\45\1\37\1"+
            "\32\1\21\1\53\1\20\1\42\1\5\1\53\1\1\1\41\3\53\1\30\1\2\1\31"+
            "\uff82\55",
            "\1\56\15\uffff\1\57",
            "\1\61",
            "\1\62",
            "\1\63\11\uffff\1\64",
            "\1\66\6\uffff\1\67\11\uffff\1\65",
            "\1\70",
            "\1\71",
            "\1\73",
            "\1\75",
            "\1\77",
            "",
            "",
            "",
            "\1\104\4\uffff\1\105",
            "",
            "\1\110",
            "\1\112\23\uffff\1\111",
            "\1\114\6\uffff\1\113\1\115",
            "\1\116",
            "\1\121\3\uffff\1\120",
            "",
            "",
            "\1\125\13\uffff\1\124",
            "",
            "",
            "\1\130\23\uffff\1\131",
            "",
            "",
            "",
            "",
            "\1\136\17\uffff\1\137",
            "",
            "\1\141",
            "\1\143\1\142",
            "\1\144",
            "\1\145",
            "\1\146",
            "\0\147",
            "\1\151\1\uffff\12\152",
            "\47\153\1\uffff\uffd8\153",
            "\1\154",
            "\32\60\4\uffff\1\60\1\uffff\32\60",
            "",
            "",
            "",
            "\1\156\5\uffff\1\157",
            "\1\160",
            "",
            "",
            "",
            "\1\161",
            "\1\162",
            "\1\163",
            "\1\164",
            "\1\166\3\uffff\1\165",
            "\1\167",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "\1\170\22\uffff\1\171",
            "\1\172",
            "\1\173",
            "\1\174",
            "\12\60\7\uffff\32\60\4\uffff\1\60\1\uffff\32\60",
            "\1\176",
            "",
            "",
            "\1\177",
            "\1\u0080",
            "",
            "",
            "\1\u0081",
            "\1\u0082",
            "",
            "",
            "\1\u0083",
            "\1\u0084",
            "",
            "",
            "",
            "",
            "\1\u0085",
            "\1\u0086",
            "",
            "\1\u0087",
            "\1\u0088",
            "\1\u0089",
            "\12\60\7\uffff\32\60\4\uffff\1\60\1\uffff\32\60",
            "\1\u008b",
            "\1\u008c",
            "",
            "",
            "",
            "\1\151\1\uffff\12\152",
            "",
            "\1\u008d",
            "",
            "\12\60\7\uffff\32\60\4\uffff\1\60\1\uffff\32\60",
            "\12\60\7\uffff\32\60\4\uffff\1\60\1\uffff\32\60",
            "\1\u0090",
            "\12\60\7\uffff\32\60\4\uffff\1\60\1\uffff\32\60",
            "\1\u0092",
            "\1\u0093",
            "\1\u0094",
            "\1\u0095",
            "\12\60\7\uffff\32\60\4\uffff\1\60\1\uffff\32\60",
            "\1\u0097",
            "\1\u0098",
            "\1\u0099",
            "\1\u009a",
            "\1\u009b",
            "\1\u009c",
            "",
            "\12\60\7\uffff\32\60\4\uffff\1\60\1\uffff\32\60",
            "\1\u009e",
            "\1\u009f",
            "\1\u00a0",
            "\1\u00a1",
            "\1\u00a2",
            "\1\u00a3",
            "\12\60\7\uffff\32\60\4\uffff\1\60\1\uffff\32\60",
            "\1\u00a5",
            "\1\u00a6",
            "\1\u00a7",
            "\1\u00a8",
            "",
            "\1\u00a9",
            "\1\u00aa",
            "\1\u00ab",
            "",
            "",
            "\12\60\7\uffff\32\60\4\uffff\1\60\1\uffff\32\60",
            "",
            "\1\u00ad",
            "\12\60\7\uffff\32\60\4\uffff\1\60\1\uffff\32\60",
            "\12\60\7\uffff\32\60\4\uffff\1\60\1\uffff\32\60",
            "\12\60\7\uffff\32\60\4\uffff\1\60\1\uffff\32\60",
            "",
            "\1\u00b1",
            "\1\u00b3\7\uffff\1\u00b2",
            "\1\u00b4",
            "\12\60\7\uffff\32\60\4\uffff\1\60\1\uffff\32\60",
            "\1\u00b6",
            "\1\u00b7",
            "",
            "\1\u00b8",
            "\12\60\7\uffff\32\60\4\uffff\1\60\1\uffff\32\60",
            "\1\u00ba",
            "\12\60\7\uffff\32\60\4\uffff\1\60\1\uffff\32\60",
            "\1\u00bc",
            "\1\u00bd",
            "",
            "\12\60\7\uffff\32\60\4\uffff\1\60\1\uffff\32\60",
            "\1\u00bf",
            "\1\u00c0",
            "\1\u00c1",
            "\1\u00c2",
            "\1\u00c3",
            "\12\60\7\uffff\32\60\4\uffff\1\60\1\uffff\32\60",
            "",
            "\1\u00c5",
            "",
            "",
            "",
            "\12\60\7\uffff\32\60\4\uffff\1\60\1\uffff\32\60",
            "\1\u00c7",
            "\1\u00c8",
            "\1\u00c9",
            "",
            "\1\u00ca",
            "\1\u00cb",
            "\12\60\7\uffff\32\60\4\uffff\1\60\1\uffff\32\60",
            "",
            "\1\u00cd",
            "",
            "\1\u00ce",
            "\1\u00cf",
            "",
            "\12\60\7\uffff\32\60\4\uffff\1\60\1\uffff\32\60",
            "\12\60\7\uffff\32\60\4\uffff\1\60\1\uffff\32\60",
            "\12\60\7\uffff\32\60\4\uffff\1\60\1\uffff\32\60",
            "\1\u00d3",
            "\1\u00d4",
            "",
            "\12\60\7\uffff\32\60\4\uffff\1\60\1\uffff\32\60",
            "",
            "\1\u00d6",
            "\1\u00d7",
            "\12\60\7\uffff\32\60\4\uffff\1\60\1\uffff\32\60",
            "\1\u00d9",
            "\12\60\7\uffff\32\60\4\uffff\1\60\1\uffff\32\60",
            "",
            "\1\u00db",
            "\12\60\7\uffff\32\60\4\uffff\1\60\1\uffff\32\60",
            "\1\u00dd",
            "",
            "",
            "",
            "\1\u00de",
            "\12\60\7\uffff\32\60\4\uffff\1\60\1\uffff\32\60",
            "",
            "\1\u00e0",
            "\1\u00e1",
            "",
            "\12\60\7\uffff\32\60\4\uffff\1\60\1\uffff\32\60",
            "",
            "\12\60\7\uffff\32\60\4\uffff\1\60\1\uffff\32\60",
            "",
            "\1\u00e4",
            "\12\60\7\uffff\32\60\4\uffff\1\60\1\uffff\32\60",
            "",
            "\1\u00e6",
            "\12\60\7\uffff\32\60\4\uffff\1\60\1\uffff\32\60",
            "",
            "",
            "\12\60\7\uffff\32\60\4\uffff\1\60\1\uffff\32\60",
            "",
            "\12\60\7\uffff\32\60\4\uffff\1\60\1\uffff\32\60",
            "",
            "",
            ""
    };

    static final short[] DFA11_eot = DFA.unpackEncodedString(DFA11_eotS);
    static final short[] DFA11_eof = DFA.unpackEncodedString(DFA11_eofS);
    static final char[] DFA11_min = DFA.unpackEncodedStringToUnsignedChars(DFA11_minS);
    static final char[] DFA11_max = DFA.unpackEncodedStringToUnsignedChars(DFA11_maxS);
    static final short[] DFA11_accept = DFA.unpackEncodedString(DFA11_acceptS);
    static final short[] DFA11_special = DFA.unpackEncodedString(DFA11_specialS);
    static final short[][] DFA11_transition;

    static {
        int numStates = DFA11_transitionS.length;
        DFA11_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA11_transition[i] = DFA.unpackEncodedString(DFA11_transitionS[i]);
        }
    }

    class DFA11 extends DFA {

        public DFA11(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 11;
            this.eot = DFA11_eot;
            this.eof = DFA11_eof;
            this.min = DFA11_min;
            this.max = DFA11_max;
            this.accept = DFA11_accept;
            this.special = DFA11_special;
            this.transition = DFA11_transition;
        }
        public String getDescription() {
            return "1:1: Tokens : ( T__14 | T__15 | T__16 | T__17 | T__18 | T__19 | T__20 | T__21 | T__22 | T__23 | T__24 | T__25 | T__26 | T__27 | T__28 | T__29 | T__30 | T__31 | T__32 | T__33 | T__34 | T__35 | T__36 | T__37 | T__38 | T__39 | T__40 | T__41 | T__42 | T__43 | T__44 | T__45 | T__46 | T__47 | T__48 | T__49 | T__50 | T__51 | T__52 | T__53 | T__54 | T__55 | T__56 | T__57 | T__58 | T__59 | T__60 | T__61 | T__62 | T__63 | T__64 | T__65 | T__66 | T__67 | T__68 | T__69 | T__70 | T__71 | RULE_STRING | RULE_DOUBLE | RULE_CHAR | RULE_NULL_TYPE | RULE_ID | RULE_INT | RULE_ML_COMMENT | RULE_SL_COMMENT | RULE_WS | RULE_ANY_OTHER );";
        }
        public int specialStateTransition(int s, IntStream _input) throws NoViableAltException {
            IntStream input = _input;
        	int _s = s;
            switch ( s ) {
                    case 0 : 
                        int LA11_0 = input.LA(1);

                        s = -1;
                        if ( (LA11_0=='v') ) {s = 1;}

                        else if ( (LA11_0=='|') ) {s = 2;}

                        else if ( (LA11_0=='&') ) {s = 3;}

                        else if ( (LA11_0=='d') ) {s = 4;}

                        else if ( (LA11_0=='t') ) {s = 5;}

                        else if ( (LA11_0=='f') ) {s = 6;}

                        else if ( (LA11_0=='=') ) {s = 7;}

                        else if ( (LA11_0=='!') ) {s = 8;}

                        else if ( (LA11_0=='<') ) {s = 9;}

                        else if ( (LA11_0=='>') ) {s = 10;}

                        else if ( (LA11_0=='+') ) {s = 11;}

                        else if ( (LA11_0=='-') ) {s = 12;}

                        else if ( (LA11_0=='*') ) {s = 13;}

                        else if ( (LA11_0=='/') ) {s = 14;}

                        else if ( (LA11_0=='%') ) {s = 15;}

                        else if ( (LA11_0=='r') ) {s = 16;}

                        else if ( (LA11_0=='p') ) {s = 17;}

                        else if ( (LA11_0=='i') ) {s = 18;}

                        else if ( (LA11_0=='.') ) {s = 19;}

                        else if ( (LA11_0=='c') ) {s = 20;}

                        else if ( (LA11_0=='[') ) {s = 21;}

                        else if ( (LA11_0==']') ) {s = 22;}

                        else if ( (LA11_0=='e') ) {s = 23;}

                        else if ( (LA11_0=='{') ) {s = 24;}

                        else if ( (LA11_0=='}') ) {s = 25;}

                        else if ( (LA11_0=='o') ) {s = 26;}

                        else if ( (LA11_0==':') ) {s = 27;}

                        else if ( (LA11_0=='(') ) {s = 28;}

                        else if ( (LA11_0==')') ) {s = 29;}

                        else if ( (LA11_0==',') ) {s = 30;}

                        else if ( (LA11_0=='n') ) {s = 31;}

                        else if ( (LA11_0==';') ) {s = 32;}

                        else if ( (LA11_0=='w') ) {s = 33;}

                        else if ( (LA11_0=='s') ) {s = 34;}

                        else if ( (LA11_0=='a') ) {s = 35;}

                        else if ( (LA11_0=='b') ) {s = 36;}

                        else if ( (LA11_0=='m') ) {s = 37;}

                        else if ( (LA11_0=='\"') ) {s = 38;}

                        else if ( ((LA11_0>='0' && LA11_0<='9')) ) {s = 39;}

                        else if ( (LA11_0=='\'') ) {s = 40;}

                        else if ( (LA11_0=='N') ) {s = 41;}

                        else if ( (LA11_0=='^') ) {s = 42;}

                        else if ( ((LA11_0>='A' && LA11_0<='M')||(LA11_0>='O' && LA11_0<='Z')||LA11_0=='_'||(LA11_0>='g' && LA11_0<='h')||(LA11_0>='j' && LA11_0<='l')||LA11_0=='q'||LA11_0=='u'||(LA11_0>='x' && LA11_0<='z')) ) {s = 43;}

                        else if ( ((LA11_0>='\t' && LA11_0<='\n')||LA11_0=='\r'||LA11_0==' ') ) {s = 44;}

                        else if ( ((LA11_0>='\u0000' && LA11_0<='\b')||(LA11_0>='\u000B' && LA11_0<='\f')||(LA11_0>='\u000E' && LA11_0<='\u001F')||(LA11_0>='#' && LA11_0<='$')||(LA11_0>='?' && LA11_0<='@')||LA11_0=='\\'||LA11_0=='`'||(LA11_0>='~' && LA11_0<='\uFFFF')) ) {s = 45;}

                        if ( s>=0 ) return s;
                        break;
                    case 1 : 
                        int LA11_38 = input.LA(1);

                        s = -1;
                        if ( ((LA11_38>='\u0000' && LA11_38<='\uFFFF')) ) {s = 103;}

                        else s = 45;

                        if ( s>=0 ) return s;
                        break;
                    case 2 : 
                        int LA11_40 = input.LA(1);

                        s = -1;
                        if ( ((LA11_40>='\u0000' && LA11_40<='&')||(LA11_40>='(' && LA11_40<='\uFFFF')) ) {s = 107;}

                        else s = 45;

                        if ( s>=0 ) return s;
                        break;
            }
            NoViableAltException nvae =
                new NoViableAltException(getDescription(), 11, _s, input);
            error(nvae);
            throw nvae;
        }
    }
 

}