package com.talosvfx.talos.editor.notifications;

import com.badlogic.gdx.utils.*;
import com.badlogic.gdx.utils.reflect.ClassReflection;
import com.badlogic.gdx.utils.reflect.ReflectionException;
import com.talosvfx.talos.editor.addons.scene.events.TalosLayerSelectEvent;
import com.talosvfx.talos.editor.addons.scene.events.TweenFinishedEvent;
import com.talosvfx.talos.editor.addons.scene.events.TweenPlayedEvent;
import com.talosvfx.talos.editor.notifications.events.*;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class Notifications {

    private static Notifications instance;

    private ObjectMap<Class<? extends Event>, Array<EventRunner>> invocationMap = new ObjectMap<>();
    private ObjectMap<Observer, ObjectMap<Class<? extends Event>, EventRunner>> observerInvocationMap = new ObjectMap<>();
    private ObjectMap<Class, Pool<Event>> eventPoolMap = new ObjectMap<>();

    private Notifications() {

    }

    public interface Observer {

    }

    public interface EventRunner {
        public void runEvent(Event event);
    }

    public interface Event extends Pool.Poolable {
        @Override
        void reset ();

        default boolean notifyThroughSocket () {
            return false;
        }

        default String getEventType () {
            return "";
        }

        default Json getAdditionalData (Json json) {
            return json;
        }

        default Json getMainData (Json json) {
            return json;
        }
    }

    private static Notifications getInstance() {
        if(instance == null) {
            instance = new Notifications();
            instance.registerEvents();
        }

        return instance;
    }

    private void registerEvents() {
        addPool(ProjectSavedEvent.class);
        addPool(NodeCreatedEvent.class);
        addPool(NodeConnectionCreatedEvent.class);
        addPool(NodeConnectionRemovedEvent.class);
        addPool(NodeDataModifiedEvent.class);
        addPool(NodeRemovedEvent.class);
        addPool(AssetFileDroppedEvent.class);
        addPool(TalosLayerSelectEvent.class);
        addPool(TweenPlayedEvent.class);
        addPool(TweenFinishedEvent.class);
    }

    public static void addEventToPool(Class<? extends Event> clazz) {
        getInstance().addPool(clazz);
    }

    private void addPool(Class clazz) {
        if(Event.class.isAssignableFrom(clazz)) {
            Pool<Event> pool = new Pool<Event>() {
                @Override
                protected Event newObject () {
                    try {
                        return (Event) ClassReflection.newInstance(clazz);
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

    public void registerObserverInner(Observer observer) {
        Method[] declaredMethods = observer.getClass().getMethods();

        for (Method method : declaredMethods) {
            EventHandler eventHandler = method.getAnnotation(EventHandler.class);

            if (eventHandler == null) continue;

            //Events should only have one param, and that param should be of instance Event
            if (method.getParameterTypes().length == 1 && Event.class.isAssignableFrom(method.getParameterTypes()[0])) {
                Class<? extends Event> event = method.getParameterTypes()[0].asSubclass(Event.class);

                if (observer.getClass().isAnonymousClass()) {
                    AccessibleObject.setAccessible(new Method[]{method}, true);
                }

                EventRunner eventRunner = new EventRunner() {
                    @Override
                    public void runEvent (Event event) {
                        try {
                            method.invoke(observer, event);
                        } catch (IllegalAccessException | InvocationTargetException e) {
                            e.printStackTrace();
                        }
                    }
                };

                if(!invocationMap.containsKey(event)) {
                    invocationMap.put(event, new Array<>());
                }
                if(!observerInvocationMap.containsKey(observer)) {
                    observerInvocationMap.put(observer, new ObjectMap<>());
                }

                Array<EventRunner> observerMethods = invocationMap.get(event);
                observerMethods.add(eventRunner);

                observerInvocationMap.get(observer).put(event, eventRunner);
            }
        }
    }

    public void fireEventInner(Event event) {
        if(invocationMap.containsKey(event.getClass())) {
            for(EventRunner eventRunner: invocationMap.get(event.getClass())) {
                eventRunner.runEvent(event);
            }
        }

        if (event.notifyThroughSocket()) {
            NotificationMessageHandler.sendEventToSocket(event);
        }

        eventPoolMap.get(event.getClass()).free(event);
    }

    public void notifyObserversInner(Array<Observer> observers, Event event) {
        for(Observer observer: observers) {
            ObjectMap<Class<? extends Event>, EventRunner> eventMap = observerInvocationMap.get(observer);
            EventRunner eventRunner = eventMap.get(event.getClass());

            eventRunner.runEvent(event);

            eventPoolMap.get(event.getClass()).free(event);
        }
    }

    public <T extends Event> T obtainEventInner(Class<T> clazz) {
        if(!eventPoolMap.containsKey(clazz)) {
            throw new GdxRuntimeException("Event is not registered for pooling: " + clazz.getSimpleName());
        }

        T eventObject = (T) eventPoolMap.get(clazz).obtain();

        return eventObject;
    }

    public void unregisterObserverInner(Observer observer) {
        if(!observerInvocationMap.containsKey(observer)) {
            return;
        }

        for(Class<? extends Event> eventClass: observerInvocationMap.get(observer).keys()) {
            EventRunner runner = observerInvocationMap.get(observer).get(eventClass);

            Array<EventRunner> eventRunners = invocationMap.get(eventClass);
            eventRunners.removeValue(runner, true);
        }

        observerInvocationMap.remove(observer);
    }

    public static void registerObserver(Observer observer) {
        getInstance().registerObserverInner(observer);
    }

    public static void fireEvent(Event event) {
        getInstance().fireEventInner(event);
    }

    public static void notifyObservers(Array<Observer> observers, Event event) {
        getInstance().notifyObserversInner(observers, event);
    }

    public static <T extends Event> T obtainEvent(Class<T> clazz) {
        return getInstance().obtainEventInner(clazz);
    }

    public static void unregisterObserver(Observer observer) {
        getInstance().unregisterObserverInner(observer);
    }
}
