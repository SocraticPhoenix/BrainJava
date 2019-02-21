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

import com.gmail.socraticphoenix.brainjava.ast.GeneralNode;
import com.gmail.socraticphoenix.brainjava.ast.Kind;
import com.gmail.socraticphoenix.brainjava.ast.Loc;
import com.gmail.socraticphoenix.brainjava.ast.Node;
import com.gmail.socraticphoenix.brainjava.ast.WhileNode;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

public class BrainfuckCompiler {
    private CharMatrix program;
    private List<Node> nodes;

    public static void compile(String val, BrainfuckVisitor visitor) throws BrainfuckCompilationException {
        BrainfuckCompiler compiler = new BrainfuckCompiler();
        compiler.parse(val);
        compiler.visit(visitor);
    }

    public void parse(String val) throws BrainfuckCompilationException {
        this.program = new CharMatrix(val);
        this.bracketCheck();

        Stack<WhileNode> loopStack = new Stack<>();
        Stack<List<Node>> nodeStack = new Stack<>();
        nodeStack.push(new ArrayList<>());
        this.program.forEach((loc, ch) -> {
            if(ch == '[') {
                WhileNode node = new WhileNode(loc, null, new ArrayList<>());
                nodeStack.peek().add(node);
                nodeStack.push(node.getNodes());
                loopStack.push(node);
            } else if (ch == ']') {
                WhileNode popped = loopStack.pop();
                popped.setEnd(loc);
                nodeStack.pop();
            } else {
                Kind kind = Kind.from(ch);
                if(kind != null) {
                    nodeStack.peek().add(new GeneralNode(loc, kind));
                }
            }
        });

        this.nodes = nodeStack.pop();
    }

    public void visit(BrainfuckVisitor compiler) throws BrainfuckCompilationException {
        compiler.visitStart();
        compiler.visitAll(this.nodes);
        compiler.visitEnd();
    }

    private void bracketCheck() throws BrainfuckCompilationException {
        Stack<Loc> brackets = new Stack<>();
        List<Loc> failed = new ArrayList<>();
        this.program.forEach((loc, ch) -> {
            if(ch == '[') {
                brackets.push(loc);
            } else if (ch == ']') {
                if(brackets.isEmpty()) {
                    failed.add(loc);
                } else {
                    brackets.pop();
                }
            }
        });
        while (!brackets.isEmpty()) {
            failed.add(brackets.pop());
        }

        if(!failed.isEmpty()) {
            throw new BrainfuckPointedCompilationException("Unbalanced brackets", this.program.insertPointers(failed::contains));
        }
    }

}
