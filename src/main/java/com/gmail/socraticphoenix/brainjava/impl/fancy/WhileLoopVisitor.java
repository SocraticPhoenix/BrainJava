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
package com.gmail.socraticphoenix.brainjava.impl.fancy;

import com.gmail.socraticphoenix.brainjava.ast.Node;
import com.gmail.socraticphoenix.brainjava.ast.WhileNode;
import com.gmail.socraticphoenix.brainjava.compiler.BrainfuckCompilationException;
import com.gmail.socraticphoenix.brainjava.compiler.BrainfuckVisitor;
import com.gmail.socraticphoenix.brainjava.impl.fancy.model.TapeModel;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.GeneratorAdapter;
import org.objectweb.asm.commons.Method;

public class WhileLoopVisitor implements BrainfuckVisitor, Opcodes {
    private ModelJavaBytecodeCompiler parent;

    private TapeModel model;
    private String cls;
    private ClassWriter cw;
    private GeneratorAdapter body;
    private GeneratorAdapter loop;
    private int n;

    public WhileLoopVisitor(ModelJavaBytecodeCompiler parent, TapeModel model, String cls, ClassWriter cw, GeneratorAdapter body, GeneratorAdapter loop, int n) {
        this.cw = cw;
        this.cls = cls;
        this.body = body;
        this.loop = loop;
        this.n = n;
        this.model = model;
        this.parent = parent;
    }

    @Override
    public void visitStart() throws BrainfuckCompilationException {
        GeneratorAdapter gen = this.loop;
        Label start = gen.newLabel();
        Label end = gen.newLabel();

        gen.mark(start);
        this.model.get(gen);
        this.model.cell().isZero(gen, end);

        gen.invokeStatic(Type.getObjectType(this.cls), new Method("loop" + this.n + "$body", Type.VOID_TYPE, new Type[0]));

        gen.goTo(start);
        gen.mark(end);
        gen.returnValue();
        gen.visitMaxs(0, 0);
        gen.visitEnd();
    }

    @Override
    public BrainfuckVisitor visitWhile(WhileNode node) throws BrainfuckCompilationException {
        int n = this.parent.nextLoopIndex();
        
        this.body.invokeStatic(Type.getObjectType(this.cls), new Method("loop" + n, Type.VOID_TYPE, new Type[0]));

        MethodVisitor nLoop = this.cw.visitMethod(ACC_PUBLIC | ACC_STATIC, "loop" + n, "()V", null, null);
        GeneratorAdapter nLoopAd = new GeneratorAdapter(nLoop, ACC_PUBLIC | ACC_STATIC, "loop" + n, "()V");

        MethodVisitor nBody = this.cw.visitMethod(ACC_PUBLIC | ACC_STATIC, "loop" + n + "$body", "()V", null, null);
        GeneratorAdapter nBodyAd = new GeneratorAdapter(nBody, ACC_PUBLIC | ACC_STATIC, "loop" + n + "$body", "()V");
        nLoopAd.visitCode();
        nBodyAd.visitCode();

        return new WhileLoopVisitor(this.parent, this.model, this.cls, this.cw, nBodyAd, nLoopAd, n);
    }

    @Override
    public void visitAdvance(Node node) throws BrainfuckCompilationException {
        this.model.advance(this.body);
    }

    @Override
    public void visitRetreat(Node node) throws BrainfuckCompilationException {
        this.model.retreat(this.body);
    }

    @Override
    public void visitIncrement(Node node) throws BrainfuckCompilationException {
        this.model.increment(this.body);
    }

    @Override
    public void visitDecrement(Node node) throws BrainfuckCompilationException {
        this.model.decrement(this.body);
    }

    @Override
    public void visitInput(Node node) throws BrainfuckCompilationException {
        this.model.input(this.body);
    }

    @Override
    public void visitOutput(Node node) throws BrainfuckCompilationException {
        this.model.output(this.body);
    }

    @Override
    public void visitEnd() throws BrainfuckCompilationException {
        this.body.returnValue();
        this.body.visitMaxs(0, 0);
        this.body.visitEnd();
    }

}
