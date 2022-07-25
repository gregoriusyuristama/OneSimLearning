/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package report;

import btc.Cloud;
import core.DTNHost;
import core.SimScenario;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 *
 * @author gregoriusyuristamanugraha
 */
public class ConfusionMatrix extends Report {

    @Override
    public void done() {
        List<DTNHost> TP = new ArrayList<DTNHost>();
        List<DTNHost> FP = new ArrayList<DTNHost>();
        List<DTNHost> TN = new ArrayList<DTNHost>();
        List<DTNHost> FN = new ArrayList<DTNHost>();
        Set<DTNHost> blacklist = Cloud.getCloudInstance().getBlacklist();
        List<DTNHost> hosts = SimScenario.getInstance().getHosts();
        for (DTNHost h : hosts) {
            if (h.toString().startsWith("Mis") && blacklist.contains(h)) {
                TN.add(h);
            } else if (h.toString().startsWith("Mis") && (!blacklist.contains(h))) {
                FP.add(h);
            } else if (h.toString().startsWith("Mes") && blacklist.contains(h)) {
                FN.add(h);
            } else if (h.toString().startsWith("Mes") && (!blacklist.contains(h))) {
                TP.add(h);
            }
        }
        double totalTP = (double) TP.size();
        double totalTN = (double) TN.size();
        double totalFP = (double) FP.size();
        double totalFN = (double) FN.size();
        write(",Messenger,Misbehave");
        write("Messenger," + TP.size() + "," + FN.size());
        write("Misbehave," + FP.size() + "," + TN.size());
        write("\nAkurasi," + ((totalTP + totalTN) / (totalTP + totalFP + totalFN + totalTN) * 100));
        write("Presisi," + (totalTP / (totalTP + totalFP)) * 100);
        super.done();
    }

}
