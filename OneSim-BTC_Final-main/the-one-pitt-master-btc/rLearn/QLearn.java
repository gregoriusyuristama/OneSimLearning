package rLearn;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
import core.DTNHost;
import core.DTNSim;
import core.Settings;
import core.SimScenario;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author gregoriusyuristamanugraha
 */
public class QLearn {

    private static QLearn QInstance = null;

    public static Map<DTNHost, Map<DTNHost, Double>> directTrust;
    public static Map<DTNHost, Double> suspension;
    public static Map<DTNHost, ArrayList<Double>> indirectTrust;
    private static final Double BSI = 43200.0; //12 jam
    private static final Double AMNESTY_FACTOR = 1800.0;

    public QLearn() {
        directTrust = new HashMap<DTNHost, Map<DTNHost, Double>>();
        suspension = new HashMap<DTNHost, Double>();
        indirectTrust = new HashMap<DTNHost, ArrayList<Double>>();

    }

    static {
        DTNSim.registerForReset(QLearn.class.getCanonicalName());
        reset();
    }

    public static void reset() {
        QInstance = null;
    }

    public static QLearn getQInstance() {
        if (QInstance == null) {
            QInstance = new QLearn();
        }
        return QInstance;
    }

    public static Map<DTNHost, ArrayList<Double>> getIndirectTrust() {
        return indirectTrust;
    }

    public static Set<DTNHost> getSuspended() {
        return suspended;
    }

    public static void setSuspended(Set<DTNHost> suspended) {
        QLearn.suspended = suspended;
    }
    public static Set<DTNHost> suspended = new HashSet<DTNHost>();

    private static void satisfiedTrx(DTNHost verificator, DTNHost host) {
        directTrust.get(verificator).put(host, directTrust.get(verificator).get(host) + verificator.getCoopFactor() * (1 - Math.abs(directTrust.get(verificator).get(host))));
        if ((suspension.get(host) - AMNESTY_FACTOR) >= 0.0) {
            suspension.put(host, suspension.get(host) - AMNESTY_FACTOR);
        } else {
            suspension.put(host, 0.0);
        }
    }

    private static void unsatisfiedTrx(DTNHost verificator, DTNHost host) {
        if ((suspension.get(host) + BSI) >= 86400.0) {
            suspension.put(host, 86400.0);
        } else {
            suspension.put(host, suspension.get(host) + BSI);
        }
        directTrust.get(verificator).put(host, directTrust.get(verificator).get(host) + verificator.getNegativeFactor() * (1 - Math.abs(directTrust.get(verificator).get(host))));
    }

    public static void updateIT(DTNHost host, DTNHost verificator) {
        ArrayList curIT = indirectTrust.get(host);
        curIT.add((directTrust.get(verificator).get(host)));
        indirectTrust.put(host, curIT);
    }

    public static void updateQ(DTNHost host, DTNHost verificator, boolean status) {
        if (status) {
            satisfiedTrx(verificator, host);
        } else {
            unsatisfiedTrx(verificator, host);
        }

    }

    public static void updateITbadACK(DTNHost host) {
//        indirectTrust.put(host, indirectTrust.get(host) + -0.5 * (1 - Math.abs(indirectTrust.get(host))));
    }

    public static void updateSus(DTNHost host) {
        if (QLearn.suspension.containsKey(host)) {
            if (QLearn.suspension.get(host) - SimScenario.getInstance().getUpdateInterval() > 0.0) {
                QLearn.suspended.add(host);
                QLearn.suspension.replace(host, QLearn.suspension.get(host) - SimScenario.getInstance().getUpdateInterval());
            } else if (QLearn.suspension.get(host) - SimScenario.getInstance().getUpdateInterval() <= 0.0) {
                QLearn.suspension.put(host, 0.0);
                QLearn.suspended.remove(host);
            }
        }
    }

    public static double getAvgIT(DTNHost h) {
        ArrayList<Double> ITList = indirectTrust.get(h);
        Double sum = 0.0;
        for (Double it : ITList) {
            sum += it;
        }
        return sum / ITList.size();
    }
}
