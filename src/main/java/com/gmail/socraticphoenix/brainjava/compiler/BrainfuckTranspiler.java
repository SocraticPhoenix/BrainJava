/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2017 socraticphoenix@gmail.com
 * Copyright (c) 2017 contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and
 * associated documentation files (the "Software"), to deal in the Software without restriction,
 * including without limitation the rights to use, copy, modify, merge, publish, distribute,
 * sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or
 * substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT
 * NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package com.gmail.socraticphoenix.brainjava.compiler;

import com.gmail.socraticphoenix.brainjava.ast.Kind;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Pattern;

public class BrainfuckTranspiler {
    private Map<String, Character> mappings;
    private Map<String, String> reverse;

    public BrainfuckTranspiler() {
        this.mappings = new LinkedHashMap<>();
        this.reverse = new LinkedHashMap<>();
    }

    public BrainfuckTranspiler put(Kind kind, String rep) {
        this.mappings.put(rep, kind.getRep());
        this.reverse.put(String.valueOf(kind.getRep()), rep);
        return this;
    }

    public String reverse(String code) {
        String resProg = "";
        Collection<String> subs = this.reverse.keySet();
        String sub;
        while (!code.isEmpty()) {
            String finalCode = code;
            if(subs.stream().noneMatch(finalCode::startsWith)) {
                code = code.substring(1);
            } else {
                break;
            }
        }
        while ((sub = subs.stream().filter(code::startsWith).findFirst().orElse(null)) != null) {
            code = code.replaceFirst(Pattern.quote(sub), "");
            resProg += this.reverse.get(sub);
            while (!code.isEmpty()) {
                String finalCode = code;
                if(subs.stream().noneMatch(finalCode::startsWith)) {
                    code = code.substring(1);
                } else {
                    break;
                }
            }
        }

        return resProg;
    }

    public String transpile(String code) {
        String resProg = "";
        Collection<String> subs = this.mappings.keySet();
        String sub;
        while (!code.isEmpty()) {
            String finalCode = code;
            if(subs.stream().noneMatch(finalCode::startsWith)) {
                code = code.substring(1);
            } else {
                break;
            }
        }
        while ((sub = subs.stream().filter(code::startsWith).findFirst().orElse(null)) != null) {
            code = code.replaceFirst(Pattern.quote(sub), "");
            resProg += String.valueOf(this.mappings.get(sub));
            while (!code.isEmpty()) {
                String finalCode = code;
                if(subs.stream().noneMatch(finalCode::startsWith)) {
                    code = code.substring(1);
                } else {
                    break;
                }
            }
        }

        return resProg;
    }

    public BrainfuckTranspiler reset() {
        this.mappings.clear();
        this.reverse.clear();
        return this;
    }

    public static BrainfuckTranspiler alphuck() {
        return new BrainfuckTranspiler()
                .put(Kind.ADVANCE, "a")
                .put(Kind.RETREAT, "c")
                .put(Kind.INCREMENT, "e")
                .put(Kind.DECREMENT, "i")
                .put(Kind.OUTPUT, "j")
                .put(Kind.INPUT, "o")
                .put(Kind.WHILE_START, "p")
                .put(Kind.WHILE_END, "s");
    }

    public static BrainfuckTranspiler ook() {
        return new BrainfuckTranspiler()
                .put(Kind.ADVANCE, "Ook. Ook?")
                .put(Kind.RETREAT, "Ook? Ook.")
                .put(Kind.INCREMENT, "Ook. Ook.")
                .put(Kind.DECREMENT, "Ook! Ook!")
                .put(Kind.OUTPUT, "Ook! Ook.")
                .put(Kind.INPUT, "Ook. Ook!")
                .put(Kind.WHILE_START, "Ook! Ook?")
                .put(Kind.WHILE_END, "Ook? Ook!");
    }

    public static BrainfuckTranspiler readable() {
        return new BrainfuckTranspiler()
                .put(Kind.ADVANCE, "advance")
                .put(Kind.RETREAT, "retreat")
                .put(Kind.INCREMENT, "increment")
                .put(Kind.DECREMENT, "decrement")
                .put(Kind.OUTPUT, "output")
                .put(Kind.INPUT, "input")
                .put(Kind.WHILE_START, "while")
                .put(Kind.WHILE_END, "end");
    }

}
