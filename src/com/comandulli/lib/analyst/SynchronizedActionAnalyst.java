package com.comandulli.lib.analyst;

import android.content.Context;
import android.os.Build;
import android.provider.Settings.Secure;
import android.util.Log;

import com.comandulli.lib.MD5;
import com.comandulli.lib.PendingValue;
import com.comandulli.lib.analyst.entity.Event;
import com.comandulli.lib.rest.RequestCallback;
import com.comandulli.lib.rest.RequestParams;
import com.comandulli.lib.rest.RequestResponse;
import com.comandulli.lib.rest.RestRequest;
import com.comandulli.lib.rest.RestRequest.RequestMethod;
import com.comandulli.lib.sqlite.ContractDatabase;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

import javax.net.ssl.HttpsURLConnection;

/**
 * The Synchronized Analyst not only acts as an ActionAnalyst {@see com.comandulli.lib.analyst.ActionAnalyst},
 * it synchornizes with a server all logs.
 * <p>
 * Server synchronization is made using enigma authentication.
 * <p>
 * Enigma authentication works in 6 steps:
 * <p>
 * Step 1. The device identify itself to the server, an requires an enigma to be solved.
 * <p>
 * Step 2. The server acknowledges its identification and sends a random enigma with an identifier.
 * <p>
 * Step 3. The device solves the enigma using a pre-defined salt.
 * The solution is defined as MD5(identification + ":" + enigma_salt + ":" + enigma_id);
 * <p>
 * Step 4. The device sends the solution to the server with the data to be processed. In our case the log.
 * <p>
 * Step 5. The server checks if the solution is valid, if it is we use the solution as a new enigma, together with another pre-defined salt,
 * it sends back a new solution defined as MD5.encode(solution + ":" + solution_salt);
 * <p>
 * Step 6. The device checks if the solution is valid, thus acknowledging that the server is trustworthy.
 * The procedure is declared as finished.
 *
 * @author <a href="mailto:caioa.comandulli@gmail.com">Caio Comandulli</a>
 * @since 1.0
 */
public class SynchronizedActionAnalyst extends ActionAnalyst {

    /**
     * The constant ENIGMA_SALT.
     */
    private String enigmaSalt = "defaultenigma";
    /**
     * The constant SOLUTION_SALT.
     */
    private String solutionSalt = "defaultsolution";

    private boolean syncing;
    private boolean resync;

    /**
     * Instantiates a new Synchronized action analyst.
     *
     * @param context          the context
     * @param contractDatabase the contract database
     */
    public SynchronizedActionAnalyst(Context context, ContractDatabase contractDatabase) {
        super(context, contractDatabase);
    }

    /**
     * Sets the salts for the enigma authentication.
     *
     * @param enigma   salt for enigma
     * @param solution salt for solution
     */
    public void setSalts(String enigma, String solution) {
        this.enigmaSalt = enigma;
        this.solutionSalt = solution;
    }

    /**
     * Method for the event logger to initialize this analyst.
     *
     * @param pending the pending
     * @param toSync  to sync
     */
    @Override
    public void init(List<Event> pending, List<Event> toSync) {
        super.init(pending, toSync);
        if (!toSync.isEmpty()) {
            synchronize();
        }
    }

    /**
     * Analyze an event.
     * <p>
     * It starts a new thread to handle an incoming event.
     * Only one analyze thread is able to run at a time.
     * All other analyze calls are queued.
     * <p>
     * On analysis it sends the event to the proper survey.
     * <p>
     * After analysis the object is {@link #synchronize()}
     *
     * @param event    the event
     * @param activity the class
     */
    @Override
    public void analyze(Event event, Class<?> activity) {
        super.analyze(event, activity);
        if (getSyncSize() > 0) {
            synchronize();
        }
    }

    /**
     * Synchronizes all events to sync.
     * <p>
     * It starts a new thread to handle a synchronization.
     * Only one synchronize thread is able to run at a time.
     * All other synchronize calls are queued.
     * <p>
     * Synchronization uses enigma authentication.
     * <p>
     * A unique identificator of this device is sent to server.
     * The server responds with an enigma to be solve.
     * <p>
     * The application solves the enigma using the enigma salt.
     * <p>
     * The enigma is sent back to the server with the event to sync.
     * The server checks if the solution is ok,
     * it encodes it again using the solution salt and respond.
     * <p>
     * This method checks if the encoding is valid,
     * thus declaring the server as trustworthy,
     * if so the event is marked as synced.
     */
    public void synchronize() {
        Runnable synchronization = new Runnable() {
            @Override
            public void run() {
                try {
                    syncing = true;
                    Event[] syncThis = getToSyncAsArray();
                    // request enigma from server
                    String serial = generateIdentifier();
                    Enigma enigma = requestEnigma(serial);
                    if (enigma != null && enigma.value != null) {
                        // check server integrity
                        String receivedEnigma = enigma.value;
                        String expectedEnigma = MD5.encode(serial + ":" + ENIGMA_SALT + ":" + enigma.id);
                        if (receivedEnigma.equals(expectedEnigma)) {
                            // solve
                            String solution = MD5.encode(receivedEnigma + ":" + SOLUTION_SALT);
                            boolean success = submitSolution(syncThis, solution, enigma.id, serial);
                            if (success) {
                                // success, remove all from database
                                for (Event event : syncThis) {
                                    removeFromSync(event);
                                }
                            }
                        }
                    }
                    if (resync) {
                        resync = false;
                        new Thread(this).start();
                    } else {
                        syncing = false;
                    }
                } catch (Exception e) {
                    Log.e("ERROR", "Caught error in sync");
                    e.printStackTrace();
                    if (EventLogger.DEBUGMODE) {
                        throw e;
                    }
                }
            }
        };
        if (!syncing) {
            resync = false;
            new Thread(synchronization).start();
        } else {
            resync = true;
        }
    }

    private class Enigma {
        /**
         * The Id.
         */
        public int id;
        /**
         * The Value.
         */
        public String value;
    }

    private Enigma requestEnigma(String serial) {
        final PendingValue<Enigma> pendingEnigma = new PendingValue<>(new Enigma());
        RequestParams params = new RequestParams();
        params.addQueryParam("serial", serial);
        RestRequest request = new RestRequest(RequestMethod.GET, "/analytics/enigma", params);
        request.execute(new RequestCallback() {
            @Override
            public void onResponse(RequestResponse response) {
                if (response != null) {
                    if (response.getCode() == HttpsURLConnection.HTTP_OK) {
                        JSONObject json = response.getContent();
                        if (json != null) {
                            try {
                                pendingEnigma.getValue().id = json.getInt("id");
                                pendingEnigma.getValue().value = json.getString("enigma");
                            } catch (JSONException e) {
                                pendingEnigma.setPending(false);
                            }
                        }
                    }
                }
                pendingEnigma.setPending(false);
            }

            @Override
            public void onInternalError() {
                // dont do shit
                pendingEnigma.setPending(false);
            }
        });
        //noinspection StatementWithEmptyBody
        while (pendingEnigma.isPending()) {
        }
        return pendingEnigma.getValue();
    }

    private boolean submitSolution(Event[] events, String solution, int id, String serial) {
        final PendingValue<Boolean> pendingBoolean = new PendingValue<>(false);
        RequestParams params = new RequestParams();
        params.addQueryParam("serial", serial);
        params.addQueryParam("solution", solution);
        params.addQueryParam("id", String.valueOf(id));
        try {
            RestRequest request = new RestRequest(RequestMethod.POST, "/analytics/solve", params);
            JSONObject json = new JSONObject();
            JSONArray array = new JSONArray();
            for (Event event : events) {
                JSONObject jsonEvent = new JSONObject();
                jsonEvent.put("code", event.getType().getCode());
                jsonEvent.put("timestamp", event.getTimestamp());
                jsonEvent.put("data", event.getData().toString());
                array.put(jsonEvent);
            }
            json.put("content", array);
            request.addContent(json);
            request.execute(new RequestCallback() {
                @Override
                public void onResponse(RequestResponse response) {
                    if (response != null) {
                        if (response.getCode() == HttpsURLConnection.HTTP_OK) {
                            pendingBoolean.setValue(true);
                        }
                    }
                    pendingBoolean.setPending(false);
                }

                @Override
                public void onInternalError() {
                    // dont do shit
                    pendingBoolean.setPending(false);
                }
            });
            //noinspection StatementWithEmptyBody
            while (pendingBoolean.isPending()) {
            }
        } catch (JSONException e) {
            return false;
        }
        return pendingBoolean.getValue();
    }

    private String generateIdentifier() {
        String identifier;
        final String deviceId = Secure.getString(getContext().getContentResolver(), Secure.ANDROID_ID);
        if (deviceId != null) {
            identifier = deviceId;
        } else {
            identifier = Build.SERIAL;
        }
        return identifier;
    }

}
