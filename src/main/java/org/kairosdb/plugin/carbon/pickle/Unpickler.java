
package org.kairosdb.plugin.carbon.pickle;

import net.razorvine.pickle.Opcodes;

import java.io.IOException;
import java.util.ArrayList;


/**
 Created with IntelliJ IDEA.
 User: bhawkins
 Date: 10/7/13
 Time: 2:14 PM
 To change this template use File | Settings | File Templates.
 */
public class Unpickler extends net.razorvine.pickle.Unpickler
{
	private boolean m_firstTuple = true;
	private int tupleOpcodeCounter=0;
	private boolean is_tuple = false;

	@Override
	protected Object dispatch(short key) throws IOException
	{
		if (key == Opcodes.TUPLE2)
		{
			if (!m_firstTuple)
			{
				m_firstTuple = true;
				//Pop three items from stack
				Object value = stack.pop();
				long time = ((Number)stack.pop()).longValue();
				String path = (String)stack.pop();

				PickleMetric metric;
				if (value instanceof Double)
					metric = new PickleMetric(path, time, (Double)value);
				else
					metric = new PickleMetric(path, time, ((Number)value).longValue());

				stack.add(metric);
			}
			else
				m_firstTuple = false;
		}
		else if ((key == Opcodes.TUPLE))
		{

			// Skip the first tuple key
			tupleOpcodeCounter++;
			if(tupleOpcodeCounter%2 == 0){
				tupleOpcodeCounter =0 ;
				return NO_RETURN_VALUE;
			}

			//Pop three items from stack
			Object value = stack.pop();
			long time = ((Number)stack.pop()).longValue();
			stack.pop();
			String path = stack.pop().toString();


			PickleMetric metric;
			if (value.toString().contains(".")){
				// Store the value as Double
				metric = new PickleMetric(path, time, Double.parseDouble(value.toString()));
			}
			else{
				// Store the value as Long
				metric = new PickleMetric(path, time, Long.parseLong(value.toString()));
			}

			stack.add(metric);

			is_tuple = true;
		}
		else if(key == Opcodes.APPEND && is_tuple){
			// Append code for tuples
			is_tuple = false;
			Object value = stack.pop();
			stack.pop();

			@SuppressWarnings("unchecked")
			ArrayList<Object> list = (ArrayList<Object>) stack.peek();
			list.add(value);

		}
		else
			return super.dispatch(key);

		return NO_RETURN_VALUE;
	}
}
