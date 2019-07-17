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

package com.twineworks.tweakflow.spec.effects.helpers;

import com.twineworks.tweakflow.spec.effects.SpecEffects;

import java.lang.reflect.InvocationTargetException;
import java.util.Objects;

public class EffectFactory {

  public static SpecEffects makeEffects(String name) {
    Class<? extends SpecEffects> clazz = ensureClassLoaded(name);
    try {
      return clazz.getDeclaredConstructor().newInstance();
    } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
      throw new RuntimeException(e.getMessage(), e);
    }
  }

  @SuppressWarnings("unchecked")
  private static Class<? extends SpecEffects> ensureClassLoaded(String name) {
    Objects.requireNonNull(name, "effect class name cannot be null");
    try {
      Class<?> aClass = EffectFactory.class.getClassLoader().loadClass(name);
      if (SpecEffects.class.isAssignableFrom(aClass)) {
        return (Class<? extends SpecEffects>) aClass;
      } else {
        throw new IllegalArgumentException("Class " + name + " does not implement the SpecEffects interface");
      }
    } catch (ClassNotFoundException e) {
      throw new RuntimeException(e.getMessage(), e);
    }

  }

}
