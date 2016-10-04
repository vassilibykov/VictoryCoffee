// Generated from Newspeak.g4 by ANTLR 4.5.3
package org.newspeaklanguage.compiler.parser;
import org.antlr.v4.runtime.Lexer;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.TokenStream;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.atn.*;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.misc.*;

@SuppressWarnings({"all", "warnings", "unchecked", "unused", "cast"})
public class NewspeakLexer extends Lexer {
	static { RuntimeMetaData.checkVersion("4.5.3", RuntimeMetaData.VERSION); }

	protected static final DFA[] _decisionToDFA;
	protected static final PredictionContextCache _sharedContextCache =
		new PredictionContextCache();
	public static final int
		T__0=1, T__1=2, T__2=3, T__3=4, CARET=5, COLON=6, COMMA=7, DOT=8, EQUAL_SIGN=9, 
		CCE_SIGN=10, LBRACKET=11, LCURLY=12, LPAREN=13, LANGLE=14, POUND=15, RANGLE=16, 
		RBRACKET=17, RCURLY=18, RPAREN=19, SEMICOLON=20, SLASH=21, VBAR=22, SPECIAL_CHAR=23, 
		BINARY_SELECTOR=24, IDENTIFIER=25, KEYWORD=26, SETTER_KEYWORD=27, BLOCK_ARG=28, 
		STRING=29, INTEGER=30, COMMENT=31, WHITESPACE=32;
	public static String[] modeNames = {
		"DEFAULT_MODE"
	};

	public static final String[] ruleNames = {
		"T__0", "T__1", "T__2", "T__3", "CARET", "COLON", "COMMA", "DOT", "EQUAL_SIGN", 
		"CCE_SIGN", "LBRACKET", "LCURLY", "LPAREN", "LANGLE", "POUND", "RANGLE", 
		"RBRACKET", "RCURLY", "RPAREN", "SEMICOLON", "SLASH", "VBAR", "SPECIAL_CHAR", 
		"BINARY_SELECTOR", "IDENTIFIER", "KEYWORD", "SETTER_KEYWORD", "BLOCK_ARG", 
		"STRING", "INTEGER", "COMMENT", "WHITESPACE"
	};

	private static final String[] _LITERAL_NAMES = {
		null, "'class'", "'public'", "'protected'", "'private'", "'^'", "':'", 
		"','", "'.'", "'='", "'::='", "'['", "'{'", "'('", "'<'", "'#'", "'>'", 
		"']'", "'}'", "')'", "';'", "'/'", "'|'"
	};
	private static final String[] _SYMBOLIC_NAMES = {
		null, null, null, null, null, "CARET", "COLON", "COMMA", "DOT", "EQUAL_SIGN", 
		"CCE_SIGN", "LBRACKET", "LCURLY", "LPAREN", "LANGLE", "POUND", "RANGLE", 
		"RBRACKET", "RCURLY", "RPAREN", "SEMICOLON", "SLASH", "VBAR", "SPECIAL_CHAR", 
		"BINARY_SELECTOR", "IDENTIFIER", "KEYWORD", "SETTER_KEYWORD", "BLOCK_ARG", 
		"STRING", "INTEGER", "COMMENT", "WHITESPACE"
	};
	public static final Vocabulary VOCABULARY = new VocabularyImpl(_LITERAL_NAMES, _SYMBOLIC_NAMES);

	/**
	 * @deprecated Use {@link #VOCABULARY} instead.
	 */
	@Deprecated
	public static final String[] tokenNames;
	static {
		tokenNames = new String[_SYMBOLIC_NAMES.length];
		for (int i = 0; i < tokenNames.length; i++) {
			tokenNames[i] = VOCABULARY.getLiteralName(i);
			if (tokenNames[i] == null) {
				tokenNames[i] = VOCABULARY.getSymbolicName(i);
			}

			if (tokenNames[i] == null) {
				tokenNames[i] = "<INVALID>";
			}
		}
	}

	@Override
	@Deprecated
	public String[] getTokenNames() {
		return tokenNames;
	}

	@Override

	public Vocabulary getVocabulary() {
		return VOCABULARY;
	}


	public NewspeakLexer(CharStream input) {
		super(input);
		_interp = new LexerATNSimulator(this,_ATN,_decisionToDFA,_sharedContextCache);
	}

	@Override
	public String getGrammarFileName() { return "Newspeak.g4"; }

	@Override
	public String[] getRuleNames() { return ruleNames; }

	@Override
	public String getSerializedATN() { return _serializedATN; }

	@Override
	public String[] getModeNames() { return modeNames; }

	@Override
	public ATN getATN() { return _ATN; }

	public static final String _serializedATN =
		"\3\u0430\ud6d1\u8206\uad2d\u4417\uaef1\u8d80\uaadd\2\"\u00c1\b\1\4\2\t"+
		"\2\4\3\t\3\4\4\t\4\4\5\t\5\4\6\t\6\4\7\t\7\4\b\t\b\4\t\t\t\4\n\t\n\4\13"+
		"\t\13\4\f\t\f\4\r\t\r\4\16\t\16\4\17\t\17\4\20\t\20\4\21\t\21\4\22\t\22"+
		"\4\23\t\23\4\24\t\24\4\25\t\25\4\26\t\26\4\27\t\27\4\30\t\30\4\31\t\31"+
		"\4\32\t\32\4\33\t\33\4\34\t\34\4\35\t\35\4\36\t\36\4\37\t\37\4 \t \4!"+
		"\t!\3\2\3\2\3\2\3\2\3\2\3\2\3\3\3\3\3\3\3\3\3\3\3\3\3\3\3\4\3\4\3\4\3"+
		"\4\3\4\3\4\3\4\3\4\3\4\3\4\3\5\3\5\3\5\3\5\3\5\3\5\3\5\3\5\3\6\3\6\3\7"+
		"\3\7\3\b\3\b\3\t\3\t\3\n\3\n\3\13\3\13\3\13\3\13\3\f\3\f\3\r\3\r\3\16"+
		"\3\16\3\17\3\17\3\20\3\20\3\21\3\21\3\22\3\22\3\23\3\23\3\24\3\24\3\25"+
		"\3\25\3\26\3\26\3\27\3\27\3\30\3\30\3\31\3\31\5\31\u008d\n\31\3\32\3\32"+
		"\7\32\u0091\n\32\f\32\16\32\u0094\13\32\3\33\3\33\3\33\3\34\3\34\3\34"+
		"\3\35\3\35\3\35\3\36\3\36\7\36\u00a1\n\36\f\36\16\36\u00a4\13\36\3\36"+
		"\3\36\3\37\6\37\u00a9\n\37\r\37\16\37\u00aa\3 \3 \3 \3 \7 \u00b1\n \f"+
		" \16 \u00b4\13 \3 \3 \3 \3 \3 \3!\6!\u00bc\n!\r!\16!\u00bd\3!\3!\3\u00b2"+
		"\2\"\3\3\5\4\7\5\t\6\13\7\r\b\17\t\21\n\23\13\25\f\27\r\31\16\33\17\35"+
		"\20\37\21!\22#\23%\24\'\25)\26+\27-\30/\31\61\32\63\33\65\34\67\359\36"+
		";\37= ?!A\"\3\2\b\5\2,-\61\61\u0080\u0080\5\2C\\aac|\6\2\62;C\\aac|\3"+
		"\2))\3\2\62;\5\2\13\f\17\17\"\"\u00c6\2\3\3\2\2\2\2\5\3\2\2\2\2\7\3\2"+
		"\2\2\2\t\3\2\2\2\2\13\3\2\2\2\2\r\3\2\2\2\2\17\3\2\2\2\2\21\3\2\2\2\2"+
		"\23\3\2\2\2\2\25\3\2\2\2\2\27\3\2\2\2\2\31\3\2\2\2\2\33\3\2\2\2\2\35\3"+
		"\2\2\2\2\37\3\2\2\2\2!\3\2\2\2\2#\3\2\2\2\2%\3\2\2\2\2\'\3\2\2\2\2)\3"+
		"\2\2\2\2+\3\2\2\2\2-\3\2\2\2\2/\3\2\2\2\2\61\3\2\2\2\2\63\3\2\2\2\2\65"+
		"\3\2\2\2\2\67\3\2\2\2\29\3\2\2\2\2;\3\2\2\2\2=\3\2\2\2\2?\3\2\2\2\2A\3"+
		"\2\2\2\3C\3\2\2\2\5I\3\2\2\2\7P\3\2\2\2\tZ\3\2\2\2\13b\3\2\2\2\rd\3\2"+
		"\2\2\17f\3\2\2\2\21h\3\2\2\2\23j\3\2\2\2\25l\3\2\2\2\27p\3\2\2\2\31r\3"+
		"\2\2\2\33t\3\2\2\2\35v\3\2\2\2\37x\3\2\2\2!z\3\2\2\2#|\3\2\2\2%~\3\2\2"+
		"\2\'\u0080\3\2\2\2)\u0082\3\2\2\2+\u0084\3\2\2\2-\u0086\3\2\2\2/\u0088"+
		"\3\2\2\2\61\u008c\3\2\2\2\63\u008e\3\2\2\2\65\u0095\3\2\2\2\67\u0098\3"+
		"\2\2\29\u009b\3\2\2\2;\u009e\3\2\2\2=\u00a8\3\2\2\2?\u00ac\3\2\2\2A\u00bb"+
		"\3\2\2\2CD\7e\2\2DE\7n\2\2EF\7c\2\2FG\7u\2\2GH\7u\2\2H\4\3\2\2\2IJ\7r"+
		"\2\2JK\7w\2\2KL\7d\2\2LM\7n\2\2MN\7k\2\2NO\7e\2\2O\6\3\2\2\2PQ\7r\2\2"+
		"QR\7t\2\2RS\7q\2\2ST\7v\2\2TU\7g\2\2UV\7e\2\2VW\7v\2\2WX\7g\2\2XY\7f\2"+
		"\2Y\b\3\2\2\2Z[\7r\2\2[\\\7t\2\2\\]\7k\2\2]^\7x\2\2^_\7c\2\2_`\7v\2\2"+
		"`a\7g\2\2a\n\3\2\2\2bc\7`\2\2c\f\3\2\2\2de\7<\2\2e\16\3\2\2\2fg\7.\2\2"+
		"g\20\3\2\2\2hi\7\60\2\2i\22\3\2\2\2jk\7?\2\2k\24\3\2\2\2lm\7<\2\2mn\7"+
		"<\2\2no\7?\2\2o\26\3\2\2\2pq\7]\2\2q\30\3\2\2\2rs\7}\2\2s\32\3\2\2\2t"+
		"u\7*\2\2u\34\3\2\2\2vw\7>\2\2w\36\3\2\2\2xy\7%\2\2y \3\2\2\2z{\7@\2\2"+
		"{\"\3\2\2\2|}\7_\2\2}$\3\2\2\2~\177\7\177\2\2\177&\3\2\2\2\u0080\u0081"+
		"\7+\2\2\u0081(\3\2\2\2\u0082\u0083\7=\2\2\u0083*\3\2\2\2\u0084\u0085\7"+
		"\61\2\2\u0085,\3\2\2\2\u0086\u0087\7~\2\2\u0087.\3\2\2\2\u0088\u0089\t"+
		"\2\2\2\u0089\60\3\2\2\2\u008a\u008d\5/\30\2\u008b\u008d\7/\2\2\u008c\u008a"+
		"\3\2\2\2\u008c\u008b\3\2\2\2\u008d\62\3\2\2\2\u008e\u0092\t\3\2\2\u008f"+
		"\u0091\t\4\2\2\u0090\u008f\3\2\2\2\u0091\u0094\3\2\2\2\u0092\u0090\3\2"+
		"\2\2\u0092\u0093\3\2\2\2\u0093\64\3\2\2\2\u0094\u0092\3\2\2\2\u0095\u0096"+
		"\5\63\32\2\u0096\u0097\7<\2\2\u0097\66\3\2\2\2\u0098\u0099\5\65\33\2\u0099"+
		"\u009a\7<\2\2\u009a8\3\2\2\2\u009b\u009c\7<\2\2\u009c\u009d\5\63\32\2"+
		"\u009d:\3\2\2\2\u009e\u00a2\7)\2\2\u009f\u00a1\n\5\2\2\u00a0\u009f\3\2"+
		"\2\2\u00a1\u00a4\3\2\2\2\u00a2\u00a0\3\2\2\2\u00a2\u00a3\3\2\2\2\u00a3"+
		"\u00a5\3\2\2\2\u00a4\u00a2\3\2\2\2\u00a5\u00a6\7)\2\2\u00a6<\3\2\2\2\u00a7"+
		"\u00a9\t\6\2\2\u00a8\u00a7\3\2\2\2\u00a9\u00aa\3\2\2\2\u00aa\u00a8\3\2"+
		"\2\2\u00aa\u00ab\3\2\2\2\u00ab>\3\2\2\2\u00ac\u00ad\7*\2\2\u00ad\u00ae"+
		"\7,\2\2\u00ae\u00b2\3\2\2\2\u00af\u00b1\13\2\2\2\u00b0\u00af\3\2\2\2\u00b1"+
		"\u00b4\3\2\2\2\u00b2\u00b3\3\2\2\2\u00b2\u00b0\3\2\2\2\u00b3\u00b5\3\2"+
		"\2\2\u00b4\u00b2\3\2\2\2\u00b5\u00b6\7,\2\2\u00b6\u00b7\7+\2\2\u00b7\u00b8"+
		"\3\2\2\2\u00b8\u00b9\b \2\2\u00b9@\3\2\2\2\u00ba\u00bc\t\7\2\2\u00bb\u00ba"+
		"\3\2\2\2\u00bc\u00bd\3\2\2\2\u00bd\u00bb\3\2\2\2\u00bd\u00be\3\2\2\2\u00be"+
		"\u00bf\3\2\2\2\u00bf\u00c0\b!\2\2\u00c0B\3\2\2\2\t\2\u008c\u0092\u00a2"+
		"\u00aa\u00b2\u00bd\3\b\2\2";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}