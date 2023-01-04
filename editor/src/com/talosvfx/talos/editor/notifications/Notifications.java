package com.talosvfx.talos.editor.notifications;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.Pool;
import com.badlogic.gdx.utils.reflect.Annotation;
import com.badlogic.gdx.utils.reflect.ClassReflection;
import com.badlogic.gdx.utils.reflect.Method;
import com.badlogic.gdx.utils.reflect.ReflectionException;
import lombok.Getter;

public class Notifications {

	private static Notifications instance;

	private ObjectMap<Class<? extends TalosEvent>, Array<EventRunner>> invocationMap = new ObjectMap<>();
	private ObjectMap<Observer, ObjectMap<Class<? extends TalosEvent>, EventRunner>> observerInvocationMap = new ObjectMap<>();
	private ObjectMap<Class, Pool<TalosEvent>> eventPoolMap = new ObjectMap<>();

	private Notifications () {

	}

	public abstract class EventRunner {

		@Getter
		private final Observer observer;

		public EventRunner (Observer observer) {
			this.observer = observer;
		}
		public abstract void runEvent (TalosEvent event);
	}

	private static Notifications getInstance () {
		if (instance == null) {
			instance = new Notifications();
			instance.registerEvents();
		}

		return instance;
	}

	private void registerEvents () {
	}

	public static void addEventToPool (Class<? extends TalosEvent> clazz) {
		getInstance().addPool(clazz);
	}

	private void addPool (Class clazz) {
		if (ClassReflection.isAssignableFrom(TalosEvent.class, clazz)) {
			Pool<TalosEvent> pool = new Pool<TalosEvent>() {
				@Override
				protected TalosEvent newObject () {
					try {
						return (TalosEvent)ClassReflection.newInstance(clazz);
					} catch (ReflectionException e) {
						throw new GdxRuntimeException(e);
					}
				}
			};
			eventPoolMap.put(clazz, pool);
		} else {
			throw new GdxRuntimeException("pool must implement Event interface");
		}
	}

	public void registerObserverInner (Observer observer) {
		final Method[] declaredMethods = ClassReflection.getMethods(observer.getClass());

		for (Method method : declaredMethods) {
			final Annotation declaredAnnotation = method.getDeclaredAnnotation(EventHandler.class);
			if (declaredAnnotation == null)
				continue;

			EventHandler eventHandler = declaredAnnotation.getAnnotation(EventHandler.class);

			if (eventHandler == null)
				continue;

			//Events should only have one param, and that param should be of instance Event
			if (method.getParameterTypes().length == 1 && ClassReflection.isAssignableFrom(TalosEvent.class, method.getParameterTypes()[0])) {
				Class<? extends TalosEvent> event = method.getParameterTypes()[0];

				method.setAccessible(true);

				EventRunner eventRunner = new EventRunner(observer) {
					@Override
					public void runEvent (TalosEvent event) {
						try {
							method.invoke(observer, event);
						} catch (ReflectionException e) {
							e.printStackTrace();
						}
					}
				};

				if (!invocationMap.containsKey(event)) {
					invocationMap.put(event, new Array<>());
				}
				if (!observerInvocationMap.containsKey(observer)) {
					observerInvocationMap.put(observer, new ObjectMap<>());
				}

				Array<EventRunner> observerMethods = invocationMap.get(event);
				observerMethods.add(eventRunner);

				observerInvocationMap.get(observer).put(event, eventRunner);
			}
		}
	}

	public void fireEventInner (TalosEvent event) {
		if (invocationMap.containsKey(event.getClass())) {
			Array<EventRunner> eventRunners = invocationMap.get(event.getClass());
			for (int i = 0; i < eventRunners.size; i++) {
				EventRunner eventRunner = eventRunners.get(i);
				testAndFireEvent(eventRunner, event);
			}
		}

		if (event.notifyThroughSocket()) {
			NotificationMessageHandler.sendEventToSocket(event);
		}

		eventPoolMap.get(event.getClass()).free(event);
	}

	private void testAndFireEvent (EventRunner eventRunner, TalosEvent event) {
		if (event instanceof ContextRequiredEvent) {
			if (!(eventRunner.getObserver() instanceof EventContextProvider)) {
				throw new GdxRuntimeException("Invalid event handler. Events that extend ContextRequiredEvent must have their " +
						"owner class implement ContextProvider");
			}

			Object eventContextObject = ((ContextRequiredEvent<?>) event).getContext();

			if (eventContextObject == null) {
				throw new GdxRuntimeException("Invalid event, context must be provided");
			}
			if (!(eventContextObject == eventRunner.getObserver())) {
				return;
			}

		}
		eventRunner.runEvent(event);
	}

	public void notifyObserversInner (Array<Observer> observers, TalosEvent event) {
		for (Observer observer : observers) {
			ObjectMap<Class<? extends TalosEvent>, EventRunner> eventMap = observerInvocationMap.get(observer);
			EventRunner eventRunner = eventMap.get(event.getClass());

			testAndFireEvent(eventRunner, event);

			eventPoolMap.get(event.getClass()).free(event);
		}
	}

	public <T extends TalosEvent> T obtainEventInner (Class<T> clazz) {
		if (!eventPoolMap.containsKey(clazz)) {
			addEventToPool(clazz);
		}

		T eventObject = (T)eventPoolMap.get(clazz).obtain();

		return eventObject;
	}

	public void unregisterObserverInner (Observer observer) {
		if (!observerInvocationMap.containsKey(observer)) {
			return;
		}

		for (Class<? extends TalosEvent> eventClass : observerInvocationMap.get(observer).keys()) {
			EventRunner runner = observerInvocationMap.get(observer).get(eventClass);

			Array<EventRunner> eventRunners = invocationMap.get(eventClass);
			eventRunners.removeValue(runner, true);
		}

		observerInvocationMap.remove(observer);
	}

	public static void registerObserver (Observer observer) {
		getInstance().registerObserverInner(observer);
	}

	public static void quickFire (Class clazz) {
		getInstance().fireEventInner(obtainEvent(clazz));
	}

	public static void fireEvent (TalosEvent event) {
		getInstance().fireEventInner(event);
	}

	public static void notifyObservers (Array<Observer> observers, TalosEvent event) {
		getInstance().notifyObserversInner(observers, event);
	}

	public static <T extends TalosEvent> T obtainEvent (Class<T> clazz) {
		return getInstance().obtainEventInner(clazz);
	}

	public static void unregisterObserver (Observer observer) {
		getInstance().unregisterObserverInner(observer);
	}
}
