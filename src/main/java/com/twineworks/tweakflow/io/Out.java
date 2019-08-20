/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2019 Twineworks GmbH
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.twineworks.tweakflow.io;

import com.twineworks.tweakflow.lang.values.Value;
import com.twineworks.tweakflow.lang.values.ValueInspector;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.WritableByteChannel;
import java.util.ArrayDeque;

import static com.twineworks.tweakflow.util.InOut.MiB;

public class Out implements AutoCloseable {

  private final WritableByteChannel channel;
  private final ByteBuffer buffer;
  final ArrayDeque<ValueSerializer> serializers;

  private final LongSerializer longSerializer = new LongSerializer();
  private final DoubleSerializer doubleSerializer = new DoubleSerializer();
  private final DecimalSerializer decimalSerializer = new DecimalSerializer();
  private final DatetimeSerializer datetimeSerializer = new DatetimeSerializer();
  private final StringSerializer stringSerializer = new StringSerializer();
  private final BinarySerializer binarySerializer = new BinarySerializer();
  private final ListSerializer listSerializer = new ListSerializer();
  private final DictSerializer dictSerializer = new DictSerializer();
  private final VoidSerializer voidSerializer = new VoidSerializer();

  public Out(WritableByteChannel channel) {
    this(channel, 8*MiB);
  }

  public Out(WritableByteChannel channel, int bufferSize) {
    this.channel = channel;
    buffer = ByteBuffer.allocate(bufferSize);
    serializers = new ArrayDeque<>();
  }

  private void toBuffer(Value v) throws IOException {
    ValueSerializer s = serializers.peek();
    s.setSubject(v);
    while(!s.put(this, buffer)){
      buffer.flip();
      channel.write(buffer);
      buffer.clear();
    }
  }

  public void write(Value v) throws IOException {
    // find the serializer in question
    if (v.isNil()){
      serializers.push(voidSerializer);
    }
    else if (v.isLongNum()){
      serializers.push(longSerializer);
    }
    else if (v.isDoubleNum()){
      serializers.push(doubleSerializer);
    }
    else if (v.isDecimal()){
      serializers.push(decimalSerializer);
    }
    else if (v.isString()){
      serializers.push(stringSerializer);
    }
    else if (v.isDateTime()){
      serializers.push(datetimeSerializer);
    }
    else if (v.isList()){
      serializers.push(listSerializer);
    }
    else if (v.isDict()){
      serializers.push(dictSerializer);
    }
    else if (v.isBinary()){
      serializers.push(binarySerializer);
    }
    else if (v.isFunction()){
      throw new IOException("Cannot serialize function values, found: "+ ValueInspector.inspect(v, true));
    }
    else {
      throw new IOException("Unknown value type: "+v.type().name());
    }

    toBuffer(v);
    serializers.pop();
  }

  public void flush() throws IOException {

    if(buffer.position() != 0){
      buffer.flip();
      channel.write(buffer);
      buffer.clear();
    }
  }

  @Override
  public void close() throws Exception {
    flush();
  }
}
