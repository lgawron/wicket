/*
 * $Id$
 * $Revision$ $Date$
 * 
 * ==================================================================== Licensed
 * under the Apache License, Version 2.0 (the "License"); you may not use this
 * file except in compliance with the License. You may obtain a copy of the
 * License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package wicket;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import wicket.model.IModel;
import wicket.util.string.StringList;

/**
 * Structure for recording {@link wicket.FeedbackMessage}s; wraps a list and
 * acts as a {@link wicket.model.IModel}.
 * 
 * @author Eelco Hillenius
 */
public final class FeedbackMessages
{
	/** Log. */
	private static Log log = LogFactory.getLog(FeedbackMessages.class);

	/**
	 * Holds a list of
	 * {@link wicket.FeedbackMessage}s.
	 */
	private List messages = null;

	/**
	 * Comparator for sorting messages on level.
	 */
	public static final class LevelComparator implements Comparator
	{
		private static final int ASCENDING = 1;
		private static final int DESCENDING = -1;

		/** Ascending / descending sign value. */
		private final int sign;

		/**
		 * Construct.
		 * 
		 * @param ascending
		 *            whether to sort ascending (otherwise, it sorts descending)
		 */
		public LevelComparator(boolean ascending)
		{
			sign = ascending ? ASCENDING : DESCENDING;
		}

		/**
		 * Compares its two arguments for order. Returns a negative integer,
		 * zero, or a positive integer as the first argument is less than, equal
		 * to, or greater than the second.
		 * <p>
		 * 
		 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
		 */
		public int compare(Object o1, Object o2)
		{
			int level1 = ((FeedbackMessage)o1).getLevel();
			int level2 = ((FeedbackMessage)o2).getLevel();
			if (level1 < level2)
			{
				return sign * -1;
			}
			else if (level1 > level2)
			{
				return sign;
			}
			else
			{
				return 0;
			}
		}
	}

	/**
	 * The {@link IModel}representation of FeedbackMessages.
	 */
	private class Model implements IModel
	{
		/**
		 * level to narrow the model to. If undefined (the default), it is not
		 * used.
		 */
		private int level = FeedbackMessage.UNDEFINED;

		/**
		 * Construct.
		 */
		public Model()
		{
		}

		/**
		 * Construct and narrow to the given level.
		 * 
		 * @param level
		 *            the level to narrow to
		 */
		public Model(final int level)
		{
			this.level = level;
		}

		/**
		 * Gets the messages.
		 * 
		 * @see wicket.model.IModel#getObject()
		 */
		public Object getObject()
		{
			if (level == FeedbackMessage.UNDEFINED)
			{
				return getMessages();
			}
			else
			{
				return getMessages(level);
			}
		}

		/**
		 * Sets the messages; the object should either be of type
		 * {@link java.util.List}or an array of {@link FeedbackMessage}s.
		 * 
		 * @see wicket.model.IModel#setObject(java.lang.Object)
		 */
		public void setObject(Object object)
		{
			if (object instanceof List)
			{
				setMessages((List)object);
			}
			else if (object instanceof FeedbackMessage[])
			{
				if (object != null)
				{
					setMessages(Arrays.asList((FeedbackMessage[])object));
				}
				else
				{
					setMessages(null);
				}
			}
			else
			{
				throw new WicketRuntimeException("Invalid model type for FeedbackMessages");
			}
		}
	}

	/**
	 * Package local constructor; clients are not allowed to create instances as
	 * this class is managed by the framework.
	 */
	FeedbackMessages()
	{
	}

	/**
	 * Adds a message.
	 * 
	 * @param message
	 *            the message
	 * @return This
	 */
	public FeedbackMessages add(FeedbackMessage message)
	{
		if (log.isDebugEnabled())
		{
			log.debug("adding message " + message + " for thread " + Thread.currentThread());
		}
		if (messages == null)
		{
			messages = new ArrayList();
		}
		messages.add(message);
		return this;
	}
    
    /**
     * Clears any existing messages
     */
    public void clear()
    {
        if (messages != null)
        {
        	messages.clear();   
        }
    }

	/**
	 * Convenience method that gets a sub list of messages with messages that
	 * are of level ERROR or above (FATAL). This is the same as calling
	 * 'getMessages(FeedbackMessage.ERROR)'.
	 * 
	 * @return the sub list of message with messages that are of level ERROR or
	 *         above, or an empty list
	 */
	public FeedbackMessages getErrorMessages()
	{
		return getMessages(FeedbackMessage.ERROR);
	}

	/**
	 * Convenience method that gets the set of reporters of messages that are of
	 * the ERROR level or above.
	 * 
	 * @return the set of reporters of messages that are of the ERROR level or
	 *         above
	 */
	public Set getErrorReporters()
	{
		if (messages != null)
		{
			final Set subset = new HashSet();
			for (final Iterator iterator = messages.iterator(); iterator.hasNext();)
			{
				final FeedbackMessage message = (FeedbackMessage)iterator.next();
				if (message.isLevel(FeedbackMessage.ERROR))
				{
					subset.add(message.getReporter());
				}
			}
			return subset;
		}
		else
		{
			return Collections.EMPTY_SET;
		}
	}

	/**
	 * Gets the list with messages (not sorted; the same ordering as they were
	 * added).
	 * 
	 * @return the list with messages
	 */
	public List getMessages()
	{
		return (this.messages != null)
				? Collections.unmodifiableList(this.messages)
				: Collections.EMPTY_LIST;
	}

	/**
	 * Gets a sub list of messages with messages that are of the given level or
	 * above.
	 * 
	 * @param level
	 *            the level to get the messages for
	 * @return the sub list of message with messages that are of the given level
	 *         or above, or an empty list
	 */
	public FeedbackMessages getMessages(int level)
	{
		if (messages != null)
		{
			final List sublist = new ArrayList();
			for (final Iterator i = messages.iterator(); i.hasNext();)
			{
				final FeedbackMessage message = (FeedbackMessage)i.next();
				if (message.isLevel(level))
				{
					sublist.add(message);
				}
			}
			return new FeedbackMessages().setMessages(sublist);
		}
		else
		{
			return new FeedbackMessages();
		}
	}

	/**
	 * Gets the list with messages sorted on level ascending (from UNDEFINED/
	 * DEBUG up to FATAL).
	 * 
	 * @return the list with messages
	 */
	public List getMessagesAscending()
	{
		return getMessagesSorted(true);
	}

	/**
	 * Gets the list with messages sorted on level descending (from FATAL down
	 * to UNDEFINED/ DEBUG).
	 * 
	 * @return the list with messages
	 */
	public List getMessagesDescending()
	{
		return getMessagesSorted(false);
	}

	/**
	 * Gets the set of reporters of messages that are of the given level or
	 * above.
	 * 
	 * @param level
	 *            the level to get the messages for
	 * @return the set of reporters of messages that are of the given level or
	 *         above
	 */
	public Set getReporters(final int level)
	{
		if (messages != null)
		{
			final Set subset = new HashSet();
			for (final Iterator iterator = messages.iterator(); iterator.hasNext();)
			{
				final FeedbackMessage message = (FeedbackMessage)iterator.next();
				if (message.isLevel(level))
				{
					subset.add(message.getReporter());
				}
			}
			return subset;
		}
		else
		{
			return Collections.EMPTY_SET;
		}
	}

	/**
	 * Convenience method that gets whether this list contains any messages with
	 * level ERROR or up. This is the same as calling
	 * 'hasMessages(FeedbackMessage.ERROR)'.
	 * 
	 * @return whether this list contains any messages with level ERROR or up
	 */
	public boolean hasErrorMessages()
	{
		return hasMessages(FeedbackMessage.ERROR);
	}

	/**
	 * Gets whether this list contains any messages.
	 * 
	 * @return whether this list contains any messages
	 */
	public boolean hasMessages()
	{
		return messages != null && !messages.isEmpty();
	}

	/**
	 * Gets whether this list contains any messages with the given level or up.
	 * 
	 * @param level
	 *            the level
	 * @return whether this list contains any messages with the given level or
	 *         up
	 */
	public boolean hasMessages(final int level)
	{
		boolean errors = false;
		if (hasMessages())
		{
			for (final Iterator iterator = messages.iterator(); iterator.hasNext();)
			{
				final FeedbackMessage message = (FeedbackMessage)iterator.next();
				if (message.isLevel(level))
				{
					errors = true;
					break;
				}
			}
		}
		return errors;
	}

	/**
	 * Convenience method to get the iterator for the currently registered
	 * messages.
	 * 
	 * @return the iterator for the currently registered messages
	 */
	public Iterator iterator()
	{
		return getMessages().iterator();
	}

	/**
	 * Gets the FeedbackMessages as an instance of {@link IModel}.
	 * 
	 * @return the FeedbackMessages as an instance of {@link IModel}
	 */
	public IModel model()
	{
		return new Model();
	}

	/**
	 * Gets the FeedbackMessages as an instance of {@link IModel}, narrowed
	 * down to the given level.
	 * 
	 * @param level
	 *            the level to narrow down to
	 * @return tthe FeedbackMessages as an instance of {@link IModel}, narrowed
	 *         down to the given level
	 */
	public IModel model(int level)
	{
		return new Model(level);
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	public String toString()
	{
		return "[feedbackMessages = " + StringList.valueOf(getMessages()) + "]";
	}

	/**
	 * Adds a new ui message with level DEBUG to the current messages.
	 * 
	 * @param reporter
	 *            the reporting component
	 * @param message
	 *            the actual message
	 */
	void debug(Component reporter, String message)
	{
		add(FeedbackMessage.debug(reporter, message));
	}

	/**
	 * Adds a new ui message with level ERROR to the current messages.
	 * 
	 * @param reporter
	 *            the reporting component
	 * @param message
	 *            the actual message
	 */
	void error(Component reporter, String message)
	{
		add(FeedbackMessage.error(reporter, message));
	}

	/**
	 * Adds a new ui message with level FATAL to the current messages.
	 * 
	 * @param reporter
	 *            the reporting component
	 * @param message
	 *            the actual message
	 */
	void fatal(Component reporter, String message)
	{
		add(FeedbackMessage.fatal(reporter, message));
	}

	/**
	 * Looks up a message for the given component.
	 * 
	 * @param component
	 *            the component to look up the message for
	 * @return the message that is found for the given component (first match)
	 *         or null if none was found
	 */
	FeedbackMessage getMessageFor(Component component)
	{
		if (messages != null)
		{
			FeedbackMessage message = null;
			for (Iterator i = messages.iterator(); i.hasNext();)
			{
				FeedbackMessage toTest = (FeedbackMessage)i.next();
				if ((toTest.getReporter() != null) && (toTest.getReporter().equals(component)))
				{
					message = toTest;
					break;
				}
			}
			return message;
		}
		else
		{
			return null;
		}
	}

	/**
	 * Convenience method that looks up whether the given component registered a
	 * message with this list with the level ERROR.
	 * 
	 * @param component
	 *            the component to look up whether it registered a message
	 * @return whether the given component registered a message with this list
	 *         with level ERROR
	 */
	boolean hasErrorMessageFor(Component component)
	{
		return hasMessageFor(component, FeedbackMessage.ERROR);
	}

	/**
	 * Looks up whether the given component registered a message with this list.
	 * 
	 * @param component
	 *            the component to look up whether it registered a message
	 * @return whether the given component registered a message with this list
	 */
	boolean hasMessageFor(Component component)
	{
		return getMessageFor(component) != null;
	}

	/**
	 * Looks up whether the given component registered a message with this list
	 * with the given level.
	 * 
	 * @param component
	 *            the component to look up whether it registered a message
	 * @param level
	 *            the level of the message
	 * @return whether the given component registered a message with this list
	 *         with the given level
	 */
	boolean hasMessageFor(Component component, int level)
	{
		FeedbackMessage message = getMessageFor(component);
		if (message != null)
		{
			return (message.isLevel(level));
		}
		else
		{
			return false;
		}
	}

	/**
	 * Adds a new ui message with level INFO to the current messages.
	 * 
	 * @param reporter
	 *            the reporting component
	 * @param message
	 *            the actual message
	 */
	void info(Component reporter, String message)
	{
		add(FeedbackMessage.info(reporter, message));
	}

	/**
	 * Adds a new ui message with level WARN to the current messages.
	 * 
	 * @param reporter
	 *            the reporting component
	 * @param message
	 *            the actual message
	 */
	void warn(Component reporter, String message)
	{
		add(FeedbackMessage.warn(reporter, message));
	}

	/**
	 * Gets the messages sorted.
	 * 
	 * @param ascending
	 *            Whether to sort ascending (true) or descending (false)
	 * @return sorted list
	 */
	private List getMessagesSorted(boolean ascending)
	{
		if (messages != null)
		{
			final List sorted = new ArrayList(messages);
			Collections.sort(sorted, new LevelComparator(ascending));
			return sorted;
		}
		else
		{
			return Collections.EMPTY_LIST;
		}
	}

	/**
	 * Sets the list with messages.
	 * 
	 * @param messages
	 *            the messages
	 * @return This
	 */
	private FeedbackMessages setMessages(List messages)
	{
		this.messages = messages;
		return this;
	}
}