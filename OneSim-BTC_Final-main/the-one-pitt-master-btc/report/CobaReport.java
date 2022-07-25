/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package report;

import core.DTNHost;
import core.UpdateListener;
import java.util.List;
import btc.Cloud;
import core.SimScenario;
import java.util.ArrayList;
import java.util.Map;
import java.util.Set;
import rLearn.QLearn;
import routing.DecisionEngineRouter;

/**
 *
 * @author gregoriusyuristamanugraha
 */
public class CobaReport extends Report {

    public CobaReport() {
        super.init();
    }

    @Override
    public void done() {
        write("Direct Trust");

        List<DTNHost> hosts = SimScenario.getInstance().getHosts();
        List<DTNHost> listHost = new ArrayList<DTNHost>();
        for (DTNHost h : hosts) {
            if (h.toString().startsWith("Mis") || h.toString().startsWith("Mes")) {
                listHost.add(h);
            }
        }
        String cetak = "";
        for (DTNHost h : listHost) {
            cetak += "," + h.toString();
        }
        write(cetak);
        for (Map.Entry<DTNHost, Map<DTNHost, Double>> entryDT : QLearn.getQInstance().directTrust.entrySet()) {
            out.print(entryDT.getKey());
            for (DTNHost h : listHost) {
                out.print("," + QLearn.getQInstance().directTrust.get(entryDT.getKey()).get(h));
            }
            write("");
        }
        write("\nIndirect Trust");
        for (Map.Entry<DTNHost, ArrayList<Double>> entryIT : QLearn.getQInstance().indirectTrust.entrySet()) {
            write(entryIT.getKey() + "," + QLearn.getQInstance().getAvgIT(entryIT.getKey()));
        }

        write("\nsuspension");
        for (Map.Entry<DTNHost, Double> entrySus : QLearn.getQInstance().suspension.entrySet()) {
            write(entrySus.getKey() + "," + entrySus.getValue());
        }
        super.done();
    }
}
