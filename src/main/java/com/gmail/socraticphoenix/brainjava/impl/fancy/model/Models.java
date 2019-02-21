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
package com.gmail.socraticphoenix.brainjava.impl.fancy.model;

import com.gmail.socraticphoenix.brainjava.impl.fancy.ModelJavaBytecodeCompiler;
import com.gmail.socraticphoenix.brainjava.impl.fancy.model.models.ArgumentIO;
import com.gmail.socraticphoenix.brainjava.impl.fancy.model.models.BoundedLongCell;
import com.gmail.socraticphoenix.brainjava.impl.fancy.model.models.IntCell;
import com.gmail.socraticphoenix.brainjava.impl.fancy.model.models.LongCell;
import com.gmail.socraticphoenix.brainjava.impl.fancy.model.models.SystemIO;
import com.gmail.socraticphoenix.brainjava.impl.fancy.model.models.UnboundedTape;

/**
 * A set of predefined models to be used in the {@link ModelJavaBytecodeCompiler}.
 */
public interface Models {

    /**
     * Creates a model with an unbounded type, 32 bit, signed, wrapping integer cells, and an interactive input.
     *
     * @param cls The name of the class the program will be compiled to.
     * @return A new model.
     */
    static TapeModel unbounded_32bit_waitInput(String cls) {
        return new UnboundedTape(cls, new IntCell(), new SystemIO(0));
    }

    /**
     * Creates a model with an unbounded type, 32 bit, signed, wrapping integer cells, and command-line-arguments input.
     *
     * @param cls The name of the class the program will be compiled to.
     * @return A new model.
     */
    static TapeModel unbounded_32bit_argInput(String cls) {
        return new UnboundedTape(cls, new IntCell(), new ArgumentIO(0));
    }

    static TapeModel unboundedTape(String cls, CellModel cellModel, IOModel ioModel) {
        return new UnboundedTape(cls, cellModel, ioModel);
    }

    static CellModel boundedCell(long min, long max) {
        if(min == Integer.MIN_VALUE && max == Integer.MAX_VALUE) {
            return new IntCell();
        } else if (min == Long.MIN_VALUE && max == Long.MAX_VALUE) {
            return new LongCell();
        } else {
            return new BoundedLongCell(max, min);
        }
    }

    static IOModel argumentIO(int eof) {
        return new ArgumentIO(eof);
    }

    static IOModel interactiveIO(int eof) {
        return new SystemIO(eof);
    }

}
