/*******************************************************************************
 * Copyright 2014 See AUTHORS file.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/

package arc.ai.utils.random;

/** @author davebaol */
public final class ConstantIntegerDistribution extends IntegerDistribution {

	public static final ConstantIntegerDistribution NEGATIVE_ONE = new ConstantIntegerDistribution(-1);
	public static final ConstantIntegerDistribution ZERO = new ConstantIntegerDistribution(0);
	public static final ConstantIntegerDistribution ONE = new ConstantIntegerDistribution(1);

	private final int value;

	public ConstantIntegerDistribution (int value) {
		this.value = value;
	}

	@Override
	public int nextInt () {
		return value;
	}

	public int getValue () {
		return value;
	}

}
