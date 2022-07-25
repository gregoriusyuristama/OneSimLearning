/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package report;

import btc.Cloud;
import core.DTNHost;
import java.util.ArrayList;
import java.util.Map;

/**
 *
 * @author gregoriusyuristamanugraha
 */
public class detectionTime extends Report {
    
    ArrayList<Double> listDetTime = new ArrayList<Double>();
    Double sumDetTime = 0.0;
    
    public detectionTime() {
        super.init();
    }

    @Override
    public void done() {
        write("host,detection time");
        for (Map.Entry<DTNHost, Double> detTime : Cloud.getCloudInstance().detectionTime.entrySet()) {
            write(detTime.getKey() + "," + detTime.getValue());
            listDetTime.add(detTime.getValue());
            sumDetTime += detTime.getValue();
        }
        write("Average : "+sumDetTime/listDetTime.size());
        super.done();
    }
}
