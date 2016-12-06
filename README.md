# Analyst for Android

Analyst makes logging ALL actions that occur in your application simple.

## Usage

### Initialization

````java
ActionAnalyst analyst = new ActionAnalyst(context, contractDatabase);
EventLogger.init(analyst);
````

First you initialize the `EventLogger`,
that allows you to log from anywhere in your application by calling its static methods.

`EventLogger` requires a `ActionAnalyst`, that handles all logging behaviour.

```java
@Override
public void onResume() {
    super.onResume();
    EventLogger.onActivityResume(this.getClass());
}

@Override
protected void onPause() {
    super.onPause();
    EventLogger.onActivityPause(this.getClass());
}
````

Example of its use, we can log the pause and resume of our `Activity`,
allowing us to know the amount of time the user is viewing an article for example.

### Surveys

`Survey` specify how the analyst will handle a group of event types.

You can tie it to an activity class with `addSurvey(Class<?>, ActionSurvey)`,
all events related to that activity will be handled by this survey.

```java
public class RegisterSurvey extends ActionSurvey {

	public static final EventType EVENT_REGISTER_STARTED = new EventType(106, "RegisterStarted", SuperType.Open);
	public static final EventType EVENT_REGISTER_CANCELLED = new EventType(206, "RegisterCancelled", SuperType.Close);
	public static final EventType EVENT_REGISTER_ENDED = new EventType(306, "RegisterEnded", SuperType.Terminated);
	public static final EventType EVENT_REGISTER_PAUSED = new EventType(406, "RegisterPaused", SuperType.Pause);
	public static final EventType EVENT_REGISTER_RESUMED = new EventType(506, "RegisterResumed", SuperType.Resume);
	.....
````

Here we implement a survey for a registration form.
We define five `EventType` and code them (personally I like to code it as you would code a HTTP Status).

```java
    .....
    public RegisterSurvey(ActionAnalyst analyst) {
		super(analyst);
		addToContained(RegisterIntroductionActivity.class.getSimpleName());
		addToContained(RegisterFormActivity.class.getSimpleName());
	}
    .....
````

At our constructor we register classes relevant to our registration form.
All activities `contained` in our `Survey` are handled by this class.

For example if our user enters either our form or the introduction we will log that a registration has started.
But if the user navigate from the introduction to the form it wont count as a registration cancelled.

```java
    .....
	@Override
	public void open(DataWrapper data) {
		analyst.addToPending(new Event(EVENT_REGISTER_STARTED, EventLogger.getCurrentTime(), data));
	}

	@Override
	public void close(DataWrapper data) {
		Event startedEvent = analyst.searchPendingEvent(EVENT_REGISTER_STARTED.getCode());
		if (startedEvent != null) {
			analyst.moveFromPendingToSync(startedEvent);
		}
		String finished = data.getValue("finished");
		if (finished != null) {
			// close
			analyst.addToSync(new Event(EVENT_REGISTER_ENDED, EventLogger.getCurrentTime(), data));
		} else {
			terminate(data);
		}
	}

	@Override
	public void terminate(DataWrapper data) {
		analyst.addToSync(new Event(EVENT_REGISTER_CANCELLED, EventLogger.getCurrentTime(), data));
	}

	@Override
	public void pause(DataWrapper data) {
		analyst.addToPending(new Event(EVENT_REGISTER_PAUSED, EventLogger.getCurrentTime(), data));
	}

	@Override
	public void resume(DataWrapper data) {
		Event pauseEvent = analyst.searchPendingEvent(EVENT_REGISTER_PAUSED.getCode());
		if (pauseEvent != null) {
			analyst.moveFromPendingToSync(pauseEvent);
		}
		analyst.addToSync(new Event(EVENT_REGISTER_RESUMED, EventLogger.getCurrentTime(), data));
	}
    .....

````

Then we can handle the specific actions that occur at an event and determine its type and how it will be processed.

### Custom Analysts

With custom analysts we can provide specific behaviour on how to handle all events.

```java
public class MyAnalyst extends SynchronizedActionAnalyst {

    private final ApplicationSurvey applicationSurvey;
    private final AuthSurvey authSurvey;
    private final RegisterSurvey registerSurvey;

    public MyAnalyst(Context context, ContractDatabase contractDatabase) {
        super(context, contractDatabase);
        applicationSurvey = new ApplicationSurvey(this, SplashScreenActivity.class);
        authSurvey = new AuthSurvey(this);
        registerSurvey = new RegisterSurvey(this);

        setDefaultSurvey(applicationSurvey);
        addSurvey(RegisterFormActivity.class, registerSurvey);
    }
    .....
````

We can define our surveys at the constructor.

```java
    .....
    @Override
    public void addToSync(Event event) {
        event.getData().putValue("device", deviceIdentifier);
        super.addToSync(event);
    }

    @Override
    public void addToPending(Event event) {
        event.getData().putValue("device", deviceIdentifier);
        super.addToPending(event);
    }

    @Override
    public void analyze(Event event, Class<?> activity) {
        if (userId != null) {
            event.getData().putValue("user", String.valueOf(userId));
        }
        super.analyze(event, activity);
    }
    .....
````

Add data relevant to all events.

At the example, every time we add an event we append the Identifier of this device.
And every time an event is analysed if we have a user, we append its Id.

### Synchronized Analysis

The `SynchronizedAnalyst` not only acts as an `ActionAnalyst`,
it synchronizes with a server all logs.

Server synchronization is made using enigma authentication.

Enigma authentication works in 6 steps:

> Step 1. The device identify itself to the server, an requires an enigma to be solved.
>
> Step 2. The server acknowledges its identification and sends a random enigma with an identifier.
>
> Step 3. The device solves the enigma using a pre-defined salt.
>   The solution is defined as MD5(identification + ":" + enigma_salt + ":" + enigma_id);
>
> Step 4. The device sends the solution to the server with the data to be processed. In our case the log.
>
> Step 5. The server checks if the solution is valid, if it is we use the solution as a new enigma, together with another pre-defined salt,
>   it sends back a new solution defined as MD5.encode(solution + ":" + solution_salt);
>
> Step 6. The device checks if the solution is valid, thus acknowledging that the server is trustworthy.
>   The procedure is declared as finished.

## Install Library

__Step 1.__ Get this code and compile it

__Step 2.__ Define a dependency within your project. For that, access to Properties > Android > Library and click on add and select the library

##  License

MIT License. See the file LICENSE.md with the full license text.

## Author

[![Caio Comandulli](https://avatars3.githubusercontent.com/u/3738961?v=3&s=150)](https://github.com/caiocomandulli "On Github")

Copyright (c) 2016 Caio Comandulli

## Third party libraries

This library uses REST Android Library by Caio Comandulli (myself), version 1.0. Copyright (C) 2016 Caio Comandulli. Licensed under MIT License.

This library uses Contracts Android Library by Caio Comandulli (myself), version 1.0. Copyright (C) 2016 Caio Comandulli. Licensed under MIT License.

## Compatibility

This Library is valid for Android systems from version Android 4.4 (android:minSdkVersion="19" android:targetSdkVersion="19").