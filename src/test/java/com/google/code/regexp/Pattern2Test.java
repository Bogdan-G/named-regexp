/**
 * Copyright (C) 2012-2013 The named-regexp Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.google.code.regexp;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;

import java.util.List;
import java.util.Map;
import org.bogdang.modifications.regex.PatternSyntaxException;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

/**
 * Tests {@link Pattern2}
 */
public class Pattern2Test {
    @Rule
    public ExpectedException thrown = ExpectedException.none();

    // REGEX-9, First test needs to check for infinite loop in
    // NamedPattern2.compile() (seen in Android) because all other
    // tests rely on it.
    @Test( timeout = 2000 )
    public void testNoInfiniteLoopInNamedPattern2Compile() {
        assertNotNull(Pattern2.compile("(?<named>x)"));
    }

    @Test
    public void testIndexOfAcceptsClassName() {
        Pattern2 p = Pattern2.compile("(?<com.example.foo>x)");
        assertEquals(0, p.indexOf("com.example.foo"));
    }

    @Test
    public void testIndexOfAcceptsNameWithSpacesAndPunctuation() {
        Pattern2 p = Pattern2.compile("(?<  Lorem ipsum dolor sit amet, consectetur adipisicing elit>x)");
        assertEquals(0, p.indexOf("  Lorem ipsum dolor sit amet, consectetur adipisicing elit"));
    }

    @Test
    public void testIndexOfAcceptsNameWithClosingAngleBracket() {
        Pattern2 p = Pattern2.compile("(?<foo bar > should not grab this bracket> x)");
        assertEquals(0, p.indexOf("foo bar "));
    }

    @Test
    public void testIndexOfAcceptsNameWithNewLines() {
        Pattern2 p = Pattern2.compile("(?<Lorem ipsum dolor sit amet,\n consectetur adipisicing elit>x)");
        assertEquals(0, p.indexOf("Lorem ipsum dolor sit amet,\n consectetur adipisicing elit"));
    }

    @Test
    public void testIndexOfNameWithUnicodeChars() {
        Pattern2 p = Pattern2.compile("(?<gefräßig>x)");
        assertEquals(0, p.indexOf("gefräßig"));
    }

    @Test
    public void testIndexOfNamedGroup() {
        Pattern2 p = Pattern2.compile("(?<named>x)");
        assertEquals(0, p.indexOf("named"));
    }

    @Test
    public void testIndexOfNamedGroupAfterUnnamedGroups() {
        Pattern2 p = Pattern2.compile("(a)(b)(?<named>x)");
        assertEquals(2, p.indexOf("named"));
    }

    @Test
    public void testIndexOfNamedGroupAfterNoncaptureGroups() {
        Pattern2 p = Pattern2.compile("(?:c)(?<named>x)");
        assertEquals(0, p.indexOf("named"));
    }

    @Test
    public void testIndexOfNamedGroupAfterUnnamedAndNoncaptureGroups() {
        Pattern2 p = Pattern2.compile("(a)(b)(?:c)(?<named>x)");
        assertEquals(2, p.indexOf("named"));
    }

    @Test
    public void testIndexOfNamedGroupAfterAnotherNamedGroup() {
        Pattern2 p = Pattern2.compile("(a)(?<foo>)(?:c)(?<named>x)");
        assertEquals(2, p.indexOf("named"));
    }

    @Test
    public void testIndexOfNestedNamedGroup() {
        Pattern2 p = Pattern2.compile("(a)(?<foo>b)(?:c)(?<bar>d(?<named>x))");
        assertEquals(3, p.indexOf("named"));
    }

    @Test
    public void testIndexOfNamedGroupAfterEscapedParen() {
        Pattern2 p = Pattern2.compile("\\(a\\)\\((b)\\)(?:c)(?<named>x)");
        assertEquals(1, p.indexOf("named"));
    }

    @Test
    public void testIndexOfNamedGroupAfterSpecialConstruct1() {
        Pattern2 p = Pattern2.compile("(?idsumx-idsumx)(?=b)(?!x)(?<named>x)");
        assertEquals(0, p.indexOf("named"));
    }

    @Test
    public void testIndexOfNamedGroupBeforeSpecialConstruct1() {
        Pattern2 p = Pattern2.compile("(?<named>x)(?idsumx-idsumx)(?=b)(?!x)");
        assertEquals(0, p.indexOf("named"));
    }

    @Test
    public void testIndexOfNamedGroupContainingSpecialConstruct() {
        Pattern2 p = Pattern2.compile("\\d{2}/\\d{2}/\\d{4}: EXCEPTION - (?<exception>(?s)(.+(?:Exception|Error)[^\\n]+(?:\\s++at [^\\n]+)++)(?:\\s*\\.{3}[^\\n]++)?\\s*)\\n");
        assertEquals(0, p.indexOf("exception"));
    }

    @Test
    public void testIndexOfNamedGroupAfterNonEscapedParenInCharacterClass() {
        Pattern2 p = Pattern2.compile("(a)(?<foo>[()])(?:c)(?<named>x)");
        assertEquals(2, p.indexOf("named"));
    }

    @Test
    public void testIndexOfNamedGroupAfterEscapedParensInCharacterClass() {
        Pattern2 p = Pattern2.compile("(a)(?<foo>[\\(\\)])(?:c)(?<named>x)");
        assertEquals(2, p.indexOf("named"));
    }

    @Test
    public void testIndexOfNamedGroupAfterEscapedOpenParenInCharacterClass() {
        Pattern2 p = Pattern2.compile("(a)(?<foo>[\\()])(?:c)(?<named>x)");
        assertEquals(2, p.indexOf("named"));
    }

    @Test
    public void testIndexOfNamedGroupAfterEscapedCloseParenInCharacterClass() {
        Pattern2 p = Pattern2.compile("(a)(?<foo>[(\\)])(?:c)(?<named>x)");
        assertEquals(2, p.indexOf("named"));
    }

    @Test
    public void testIndexOfNamedGroupAfterSlashedParensInCharacterClass() {
        // double-slashes in a character class are literal slashes, not escapes
        Pattern2 p = Pattern2.compile("(a)(?<foo>[\\\\(\\\\)])(?:c)(?<named>x)");
        assertEquals(2, p.indexOf("named"));
    }

    @Test
    public void testIndexOfNamedGroupAfterSlashedOpenParenInCharacterClass() {
        // double-slashes in a character class are literal slashes, not escapes
        Pattern2 p = Pattern2.compile("(a)(?<foo>[\\\\()])(?:c)(?<named>x)");
        assertEquals(2, p.indexOf("named"));
    }

    @Test
    public void testIndexOfNamedGroupAfterSlashedCloseParenInCharacterClass() {
        // double-slashes in a character class are literal slashes, not escapes
        Pattern2 p = Pattern2.compile("(a)(?<foo>[(\\\\)])(?:c)(?<named>x)");
        assertEquals(2, p.indexOf("named"));
    }

    @Test
    public void testIndexOfNamedGroupAfterNonEscapedParenInCharClassWithEscapedCloseBracket() {
        Pattern2 p = Pattern2.compile("(a)(?<foo>[\\]()])(?:c)(?<named>x)");
        assertEquals(2, p.indexOf("named"));
    }

    @Test
    public void testIndexOfNamedGroupAfterNonEscapedParenAfterEscapedOpenBracket() {
        // since the open-bracket is escaped, it doesn't create a character class,
        // so the parentheses inside the "foo" group is a capturing group (that
        // currently captures nothing but still valid regex and thus counted)
        Pattern2 p = Pattern2.compile("(a)(?<foo>\\[()])(?:c)(?<named>x)");
        assertEquals(3, p.indexOf("named"));
    }

    @Test
    public void testIndexOfNotFound() {
        Pattern2 p = Pattern2.compile("(a)(b)(?:c)(?<named>x)");
        assertEquals(-1, p.indexOf("dummy"));
    }

    @Test
    public void testIndexOfWithPositiveLookbehind() {
        Pattern2 p = Pattern2.compile("(a)(b)(?<=c)(?<named>x)");
        assertEquals(2, p.indexOf("named"));
    }

    @Test
    public void testIndexOfWithNegativeLookbehind() {
        Pattern2 p = Pattern2.compile("(a)(b)(?<!c)(?<named>x)");
        assertEquals(2, p.indexOf("named"));
    }

    @Test
    public void testIndexOfWithNegativeLookbehindAtBeginning() {
        Pattern2 p = Pattern2.compile("(?<!a)(b)(c)(?<named>x)");
        assertEquals(2, p.indexOf("named"));
    }

    @Test
    public void testIndexOfWithPositiveLookbehindAtBeginning() {
        Pattern2 p = Pattern2.compile("(?<=a)(b)(c)(?<named>x)");
        assertEquals(2, p.indexOf("named"));
    }

    @Test
    public void testIndexOfWithPositiveLookahead() {
        Pattern2 p = Pattern2.compile("(a)(b)(?=c)(?<named>x)");
        assertEquals(2, p.indexOf("named"));
    }

    @Test
    public void testIndexOfWithNegativeLookahead() {
        Pattern2 p = Pattern2.compile("(a)(b)(?!c)(?<named>x)");
        assertEquals(2, p.indexOf("named"));
    }

    @Test
    public void testIndexOfWithFlags() {
        Pattern2 p = Pattern2.compile("(a)(b)(?idsumx)(?<named>x)");
        assertEquals(2, p.indexOf("named"));
    }

    @Test
    public void testIndexOfWithFlagsAndExtraNoCapture() {
        Pattern2 p = Pattern2.compile("(a)(b)(?idsumx:Z)(?<named>x)");
        assertEquals(2, p.indexOf("named"));
    }

    @Test
    public void testIndexOfNamedGroupAtBeginning() {
        Pattern2 p = Pattern2.compile("(?<named>x)(a)(b)(?:c)");
        assertEquals(0, p.indexOf("named"));
    }

    @Test
    public void testIndexOfNamedGroupAtMiddle() {
        Pattern2 p = Pattern2.compile("(a)(?<named>x)(b)(?:c)");
        assertEquals(1, p.indexOf("named"));
    }

    @Test
    public void testIndexOfWithMultipleGroupsWithSameName() {
        Pattern2 p = Pattern2.compile("(a)(?<named>x)(b)(?:c)(?<named>y)");
        assertEquals(3, p.indexOf("named", 1));
    }

    @Test
    public void testIndexOfWithInvalidPositiveInstanceIndex() {
        Pattern2 p = Pattern2.compile("(a)(?<named>x)(b)(?:c)(?<named>y)");
        thrown.expect(IndexOutOfBoundsException.class);
        thrown.expectMessage("Index: 10000000, Size: 2");
        assertEquals(-1, p.indexOf("named", 10000000));
    }

    @Test
    public void testIndexOfWithInvalidNegativeInstanceIndex() {
        Pattern2 p = Pattern2.compile("(a)(?<named>x)(b)(?:c)(?<named>y)");
        // Negative index causes ArrayIndexOutOfBoundsException (which
        // is a subclass of IndexOutOfBoundsException)
        thrown.expect(ArrayIndexOutOfBoundsException.class);
        thrown.expectMessage("-100");
        assertEquals(-1, p.indexOf("named", -100));
    }

    @Test
    public void testIndexOfNamedGroupAfterQuoteEscapedBracket() {
        // open-bracket escaped, so it's not a character class
        Pattern2 p = Pattern2.compile("(a)(b)\\Q[\\E(?<named>c)\\]");
        assertEquals(2, p.indexOf("named"));
    }

    @Test
    public void testIndexOfNamedGroupAfterSlashEscapedBracket() {
        // open-bracket escaped, so it's not a character class
        Pattern2 p = Pattern2.compile("(a)(b)\\[(?<named>c)\\]");
        assertEquals(2, p.indexOf("named"));
    }

    @Test
    public void testIndexOfNamedGroupAfterQuoteEscapedPattern2() {
        // The quote-escaped string looks like a real regex Pattern2, but
        // it's a literal string, so ignore it. The Pattern2 after that
        // should still be found
        Pattern2 p = Pattern2.compile("(?<foo>a)\\Q(?<bar>b)(?<baz>c)(d)  [  \\E(?<named>e)  \\Q]\\E");
        assertEquals(1, p.indexOf("named"));
    }

    @Test
    public void testIndexOfNamedGroupInEscapedQuote() {
        // since quote-escape is itself escaped, it's actually a literal \Q and \E
        Pattern2 p = Pattern2.compile("(a)\\\\Q(?<named>\\d+)\\\\E");
        assertEquals(1, p.indexOf("named"));
    }

    @Test
    public void testInvalidCloseQuoteEscapeSequence() {
        // when \E present, \Q must also be present, so the following is invalid syntax
        thrown.expect(PatternSyntaxException.class);
        Pattern2.compile("(a)\\\\Q(?<named>d)\\E");
    }

    @Test
    public void testNamedPattern2GetsOriginalPattern2() {
        final String ORIG_PATT = "(a)(b)(?:c)(?<named>x)";
        Pattern2 p = Pattern2.compile(ORIG_PATT);
        assertEquals(ORIG_PATT, p.namedPattern());
    }

    @Test
    public void testStandardPattern2GetsOrigWithoutNamed() {
        final String ORIG_PATT = "(a)(b)(?:c)(?<named>x)";
        final String PATT_W_NO_NAMED_GRPS = "(a)(b)(?:c)(x)";
        Pattern2 p = Pattern2.compile(ORIG_PATT);
        assertEquals(PATT_W_NO_NAMED_GRPS, p.standardPattern());
    }

    @Test
    public void testNamedPattern2AfterFlagsAndLookarounds() {
        final String ORIG_PATT = "(?idsumx-idsumx)(?=b)(?!x)(?<named>x)";
        Pattern2 p = Pattern2.compile(ORIG_PATT);
        assertEquals(ORIG_PATT, p.namedPattern());
    }

    @Test
    public void testNamedPattern2AfterEscapedParen() {
        final String ORIG_PATT = "\\(a\\)\\((b)\\)(?:c)(?<named>x)";
        Pattern2 p = Pattern2.compile(ORIG_PATT);
        assertEquals(ORIG_PATT, p.namedPattern());
    }

    @Test
    public void testGroupNames() {
        final String PATT = "(foo)(?<X>a)(?<Y>b)(?<Z>c)(bar)";
        Pattern2 p = Pattern2.compile(PATT);
        assertNotNull(p.groupNames());
        assertEquals(3, p.groupNames().size());
        assertEquals("X", p.groupNames().get(0));
        assertEquals("Y", p.groupNames().get(1));
        assertEquals("Z", p.groupNames().get(2));
    }

    @Test
    public void testGroupInfoMapHasNamesAsKeys() {
        final String PATT = "(foo)(?<X>a)(?<Y>b)(bar)(?<Z>c)(?<Z>d)"; // two groups named "Z"
        Pattern2 p = Pattern2.compile(PATT);
        Map<String,List<GroupInfo> > map = p.groupInfo();
        assertNotNull(map);
        assertEquals(3, map.size());
        assertTrue(map.containsKey("X"));
        assertTrue(map.containsKey("Y"));
        assertTrue(map.containsKey("Z"));
    }

    @Test
    public void testGroupInfoMapHasCorrectPosAndGroupIndex() {
        final String PATT = "(foo)(?<X>a)(?<Y>b)(bar)(?<Z>c)(?<Z>d)"; // two groups named "Z"
        Pattern2 p = Pattern2.compile(PATT);
        Map<String,List<GroupInfo> > map = p.groupInfo();
        assertNotNull(map);

        GroupInfo[] inf = (GroupInfo[])map.get("X").toArray(new GroupInfo[0]);
        assertEquals(1, inf.length);
        assertEquals(PATT.indexOf("(?<X>"), inf[0].pos());
        assertEquals(1, inf[0].groupIndex());

        GroupInfo[] inf2 = (GroupInfo[])map.get("Y").toArray(new GroupInfo[0]);
        assertEquals(1, inf2.length);
        assertEquals(PATT.indexOf("(?<Y>"), inf2[0].pos());
        assertEquals(2, inf2[0].groupIndex());

        // test both Z groups
        GroupInfo[] inf3 = (GroupInfo[])map.get("Z").toArray(new GroupInfo[0]);
        assertEquals(2, inf3.length);
        int posZ = PATT.indexOf("(?<Z>");
        assertEquals(posZ, inf3[0].pos());
        assertEquals(4, inf3[0].groupIndex());
        assertEquals(PATT.indexOf("(?<Z>", posZ+1), inf3[1].pos());
        assertEquals(5, inf3[1].groupIndex());
    }

    @Test(expected = PatternSyntaxException.class)
    public void testEscapedLeftParenCausesPattern2Exception() {
        final String PATT = "\\(?<name>abc)";
        Pattern2.compile(PATT);
    }

    @Test
    public void testIgnoresPattern2WithEscapedParens() {
        final String PATT = "\\(?<name>abc\\)";
        Pattern2 p = Pattern2.compile(PATT);
        assertEquals(PATT, p.standardPattern());
    }

    @Test
    public void testTakesPattern2WithEscapedEscape() {
        // it looks like an escaped parenthesis, but the escape char is
        // itself escaped and is thus a literal
        final String PATT = "\\\\(?<name>abc)";
        Pattern2 p = Pattern2.compile(PATT);
        assertEquals("\\\\(abc)", p.standardPattern());
    }

    @Test
    public void testIgnoresPattern2WithOddNumberEscapes() {
        final String PATT = "\\\\\\(?<name>abc\\)";
        Pattern2 p = Pattern2.compile(PATT);
        assertEquals(PATT, p.standardPattern());
    }

    @Test
    public void testTakesPattern2WithOddNumberEscapesButWithSpace() {
        final String PATT = "\\ \\\\(?<name>abc)";
        Pattern2 p = Pattern2.compile(PATT);
        assertEquals("\\ \\\\(abc)", p.standardPattern());
    }

    @Test
    public void testCompileRegexWithFlags() {
        final String PATT = "(?<name>abc) # comment 1";
        int flags = Pattern2.CASE_INSENSITIVE | Pattern2.COMMENTS;
        Pattern2 p = Pattern2.compile(PATT, flags);
        assertEquals(PATT, p.namedPattern());
        assertEquals(flags, p.flags());
    }

    @Test
    public void testSplitGetsArrayOfTextAroundMatches() {
        Pattern2 p = Pattern2.compile("(a)(b)(?:c)(?<named>x)");
        assertArrayEquals(new String[]{"foo ", " bar "}, p.split("foo abcx bar abcx"));
        // when the limit is specified, the last element contains
        // the remainder of the string
        assertArrayEquals(new String[]{"foo ", " bar abcx"}, p.split("foo abcx bar abcx", 2));
    }

    @Test
    public void testEqualsNullGetsFalse() {
        Pattern2 p = Pattern2.compile("(a)(b)(?:c)(?<named>x)");
        assertFalse(p.equals(null));
    }

    @Test
    public void testEqualsDiffDataTypeGetsFalse() {
        Pattern2 p = Pattern2.compile("(a)(b)(?:c)(?<named>x)");
        assertFalse(p.equals(new Object()));
    }

    @Test
    public void testEqualsWithSamePattern2AndFlagsGetsTrue() {
        Pattern2 p1 = Pattern2.compile("(a)(b)(?:c)(?<named>x)");
        Pattern2 p2 = Pattern2.compile("(a)(b)(?:c)(?<named>x)");
        assertTrue(p1.equals(p2));
    }

    @Test
    public void testEqualsWithSamePattern2ButDiffFlagsGetsFalse() {
        Pattern2 p1 = Pattern2.compile("(a)(b)(?:c)(?<named>x)");
        Pattern2 p2 = Pattern2.compile("(a)(b)(?:c)(?<named>x)", Pattern2.CASE_INSENSITIVE);
        assertFalse(p1.equals(p2));
    }

    @Test
    public void testEqualsWithSameFlagsButDiffPattern2GetsFalse() {
        Pattern2 p1 = Pattern2.compile("(a)(b)(?:c)(?<named>x)", Pattern2.DOTALL);
        Pattern2 p2 = Pattern2.compile("(?<named>x)", Pattern2.DOTALL);
        assertFalse(p1.equals(p2));
    }

    @Test
    public void testEqualsGetsTrueForSameInstance() {
        Pattern2 p = Pattern2.compile("(a)(b)(?:c)(?<named>x)");
        assertTrue(p.equals(p));
    }

    @Test
    public void testToString() {
        String s = Pattern2.compile("(a)(b)(?:c)(?<named>x)").toString();
        assertNotNull(s);
        assertTrue(s.trim().length() > 0);
    }

    @Test
    public void testCompileWithBackrefGetsStandardPattern2WithCorrectGroupIndex() {
        Pattern2 p = Pattern2.compile("(?<foo>xyz)(?<bar>\\d+)abc\\k<bar>");
        assertEquals("(xyz)(\\d+)abc\\2", p.standardPattern());
    }

    @Test
    public void testCompileWithUnknownBackref() {
        thrown.expect(PatternSyntaxException.class);
        thrown.expectMessage("unknown group name near index 11\n" +
                             "(xyz)abc\\k<bar>\n" +
                             "           ^");
        Pattern2.compile("(?<foo>xyz)abc\\k<bar>");
    }

    @Test
    public void testCompileWithEscapedBackref() {
        // escaped backrefs are not translated
        Pattern2 p = Pattern2.compile("(?<foo>xyz)(?<bar>\\d+)abc\\\\k<bar>");
        assertEquals("(xyz)(\\d+)abc\\\\k<bar>", p.standardPattern());
    }

    @Test
    public void testCompileBackrefAcceptsClassName() {
        String GROUP_NAME = "com.example.foo";
        Pattern2 p = Pattern2.compile("(?<foo>xyz)(?<" + GROUP_NAME + ">\\d+)abc\\k<" + GROUP_NAME + ">");
        assertEquals("(xyz)(\\d+)abc\\2", p.standardPattern());
    }

    @Test
    public void testCompileBackrefAcceptsNameWithSpacesAndPunctuation() {
        String GROUP_NAME = "  Lorem ipsum dolor sit amet, consectetur adipisicing elit";
        Pattern2 p = Pattern2.compile("(?<foo>xyz)(?<" + GROUP_NAME + ">\\d+)abc\\k<" + GROUP_NAME + ">");
        assertEquals("(xyz)(\\d+)abc\\2", p.standardPattern());
    }

    @Test
    public void testCompileBackrefTakesFirstClosingAngleBracket() {
        String GROUP_NAME = "foo bar  >";
        Pattern2 p = Pattern2.compile("(?<foo>xyz)(?<" + GROUP_NAME + ">\\d+)abc\\k<" + GROUP_NAME + ">");
        // The first closing bracket encountered is used. The second becomes a literal,
        // so we check for it in the standard Pattern2 (two actually).
        assertEquals("(xyz)(>\\d+)abc\\2>", p.standardPattern());
    }

    @Test
    public void testCompileBackrefAcceptsNameWithNewLines() {
        String GROUP_NAME = "Lorem ipsum dolor sit amet,\n consectetur adipisicing elit";
        Pattern2 p = Pattern2.compile("(?<foo>xyz)(?<" + GROUP_NAME + ">\\d+)abc\\k<" + GROUP_NAME + ">");
        assertEquals("(xyz)(\\d+)abc\\2", p.standardPattern());
    }

    @Test
    public void testCompileBackrefAcceptsNameWithUnicodeChars() {
        String GROUP_NAME = "gefräßig";
        Pattern2 p = Pattern2.compile("(?<foo>xyz)(?<" + GROUP_NAME + ">\\d+)abc\\k<" + GROUP_NAME + ">");
        assertEquals("(xyz)(\\d+)abc\\2", p.standardPattern());
    }
}
