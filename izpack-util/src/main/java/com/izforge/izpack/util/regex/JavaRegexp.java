/*
 * IzPack - Copyright 2001-2010 Julien Ponge, All Rights Reserved.
 *
 * http://izpack.org/
 * http://izpack.codehaus.org/
 *
 * Copyright 2010 Rene Krell
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.izforge.izpack.util.regex;

import java.util.regex.*;

import org.apache.tools.ant.BuildException;

/***
 * Regular expression implementation using the JDK 1.4 regular expression package
 *
 * @see <a href='http://ant.apache.org'>Apache Ant</a>
 * @author René Krell - changes against the original implementation in Apache Ant 1.6.5
 */
public class JavaRegexp extends JavaRegexpMatcher implements Regexp {

    /** Constructor for Jdk14RegexpRegexp */
    public JavaRegexp() {
        super();
    }

    /**
     * Convert ant regexp substitution option to jdk1.4 options.
     *
     * @param options the ant regexp options
     * @return the jdk14 substition options
     */
    protected int getSubsOptions(int options) {
        int subsOptions = REPLACE_FIRST;
        if (RegexUtil.hasFlag(options, REPLACE_ALL)) {
            subsOptions = REPLACE_ALL;
        }
        return subsOptions;
    }

    /**
     * Perform a substitution on the regular expression.
     * @param input The string to substitute on
     * @param argument The string which defines the substitution
     * @param options The list of options for the match and replace.
     * @return the result of the operation
     * @throws BuildException on error
     */
    public String substitute(String input, String argument, int options)
    throws RuntimeException
    {
        // translate \1 to $(1) so that the Matcher will work
        StringBuffer subst = new StringBuffer();
        for (int i = 0; i < argument.length(); i++) {
            char c = argument.charAt(i);
            if (c == '$') {
                subst.append('\\');
                subst.append('$');
            } else if (c == '\\') {
                if (++i < argument.length()) {
                    c = argument.charAt(i);
                    int value = Character.digit(c, 10);
                    if (value > -1) {
                        subst.append("$").append(value);
                    } else {
                        subst.append(c);
                    }
                } else {
                    // XXX - should throw an exception instead?
                    subst.append('\\');
                }
            } else {
                subst.append(c);
            }
        }
        argument = subst.toString();

        int sOptions = getSubsOptions(options);
        Pattern p = getCompiledPattern(options);
        StringBuffer sb = new StringBuffer();

        Matcher m = p.matcher(input);
        if (RegexUtil.hasFlag(sOptions, REPLACE_ALL)) {
            sb.append(m.replaceAll(argument));
        } else {
            boolean res = m.find();
            if (res) {
                m.appendReplacement(sb, argument);
                m.appendTail(sb);
            } else {
                sb.append(input);
            }
        }

        return sb.toString();
    }
}
