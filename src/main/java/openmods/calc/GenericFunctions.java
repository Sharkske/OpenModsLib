package openmods.calc;

import com.google.common.base.Optional;
import com.google.common.collect.Lists;
import java.util.List;
import openmods.utils.Stack;

public class GenericFunctions {

	public abstract static class AccumulatorFunction<E> implements ISymbol<E> {
		private final E nullValue;

		public AccumulatorFunction(E nullValue) {
			this.nullValue = nullValue;
		}

		@Override
		public void execute(ICalculatorFrame<E> frame, Optional<Integer> argumentsCount, Optional<Integer> returnsCount) {
			if (returnsCount.isPresent() && returnsCount.get() != 1) throw new StackValidationException("Invalid expected return values count");

			final Stack<E> stack = frame.stack();
			final int args = argumentsCount.or(2);

			if (args == 0) {
				stack.push(nullValue);
			} else {
				E result = stack.pop();

				for (int i = 1; i < args; i++) {
					final E value = stack.pop();
					result = accumulate(result, value);
				}

				stack.push(process(result, args));
			}
		}

		protected E process(E result, int argCount) {
			return result;
		}

		protected abstract E accumulate(E result, E value);
	}

	public static <E> void createStackManipulationFunctions(TopFrame<E> topFrame) {
		topFrame.setSymbol("swap", new FixedSymbol<E>(2, 2) {
			@Override
			public void execute(ICalculatorFrame<E> frame) {
				final Stack<E> stack = frame.stack();

				final E first = stack.pop();
				final E second = stack.pop();

				stack.push(first);
				stack.push(second);
			}
		});

		topFrame.setSymbol("pop", new ISymbol<E>() {
			@Override
			public void execute(ICalculatorFrame<E> frame, Optional<Integer> argumentsCount, Optional<Integer> returnsCount) {
				if (returnsCount.isPresent() && returnsCount.get() != 0) throw new StackValidationException("Invalid expected return values on 'pop'");

				final Stack<E> stack = frame.stack();

				final int count = argumentsCount.or(1);
				for (int i = 0; i < count; i++)
					stack.pop();
			}
		});

		topFrame.setSymbol("dup", new ISymbol<E>() {
			@Override
			public void execute(ICalculatorFrame<E> frame, Optional<Integer> argumentsCount, Optional<Integer> returnsCount) {
				final Stack<E> stack = frame.stack();

				List<E> values = Lists.newArrayList();

				final int in = argumentsCount.or(1);
				for (int i = 0; i < in; i++) {
					final E value = stack.pop();
					values.add(value);
				}

				values = Lists.reverse(values);

				final int out = returnsCount.or(2 * in);
				for (int i = 0; i < out; i++) {
					final E value = values.get(i % in);
					stack.push(value);
				}
			}
		});
	}

}