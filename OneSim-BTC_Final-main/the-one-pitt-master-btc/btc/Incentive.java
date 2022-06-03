/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package btc;

import core.DTNHost;
import core.Message;
import core.SimClock;
import core.SimScenario;
import core.Tuple;
import core.UpdateListener;
import input.RumusMatematika;
import java.security.PublicKey;
import java.util.*;
import javax.crypto.Cipher;
import net.sourceforge.jFuzzyLogic.FIS;
import net.sourceforge.jFuzzyLogic.FunctionBlock;
import routing.DecisionEngineRouter;
import routing.MessageRouter;
import routing.RoutingDecisionEngine;
import routing.community.EncounterDetectionEngine;
import rLearn.QLearn;

/**
 *
 * @author WINDOWS_X
 */
public class Incentive {

    private static boolean blacklistActive = true;

    private static Map<Message, List<DTNHost>> ack = new HashMap<Message, List<DTNHost>>();
    private static Map<String, Tuple<Transaction, Boolean>> deposits = new HashMap<String, Tuple<Transaction, Boolean>>();
    private static Map<Message, Map<DTNHost, Set<String>>> verificating = new HashMap<Message, Map<DTNHost, Set<String>>>();
    private static Map<DTNHost, Set<byte[]>> trustToken = new HashMap<DTNHost, Set<byte[]>>();
    private static Map<Message, Set<String>> pending = new HashMap<Message, Set<String>>();

    public static Map<DTNHost, Double> detectionTime = new HashMap<DTNHost, Double>();

    private static Map<DTNHost, List<DTNHost>> detectedAccomplice = new HashMap<DTNHost, List<DTNHost>>();

    private static Set<Message> finished = new HashSet<Message>();

    public static Set<Message> getFinished() {
        return finished;
    }
    private static Set<DTNHost> blacklist = new HashSet<DTNHost>();

    public Incentive() {
    }

    public static void setAck(Message m, Map<DTNHost, PublicKey> publicKeys) {
        int in = 0;
        //baca node yang dilewati pesan
        List<DTNHost> nodes = m.getHops();
        //ambil signatures yang diberikan di pesan
        List<byte[]> signatures = (List<byte[]>) m.getProperty("signatures");
        //membuat list untuk menampung host yang sudah diverifikasi
        List<DTNHost> verified = new ArrayList<DTNHost>();

        //membaca semua host di dalam nodes (node yang dilewati pesan)
        for (DTNHost host : nodes) {
            //mengecualikan node pertama (pembuat pesan) dan node terakhir (tujuan)
            if (in > 0 && (in < nodes.size() - 1)) {

                /*
                jika wallet dari node yang dilewati sesuai dengan
                wallet yang dicatat di pesan maka wallet dicatat
                ke dalam verified list
                 */
                String validation = m.toString() + host.toString();

                try {
                    String signature = do_RSADecryption(signatures.get(in), publicKeys.get(host));
                    if (signature.matches(validation)) {
//                        System.out.println("verified : "+host);
                        verified.add(host);
                    } else {
//                        System.out.println("cek : " + host);
                        QLearn.updateITbadACK(host);
//                        if (blacklistActive) {
////                            blacklist.add(host);
//                        }
                    }
                } catch (Exception ex) {
                    QLearn.updateITbadACK(host);
//                    if (blacklistActive) {
////                        blacklist.add(host);
//                    }
                }
            }
            //index naik untuk membaca isi list wallet dari awal hingga akhir
            in++;
        }
        ack.put(m, verified);
    }

    public static void setTrustToken(Map.Entry<DTNHost, Set<byte[]>> tToken, DTNHost sender, DTNHost verificator, Map<DTNHost, PublicKey> publicKeys) {
        //membaca pesan dari List messages
        DTNHost host = tToken.getKey();
        Set<byte[]> messages = tToken.getValue();
//        QLearn.initDT(verificator, sender);
        for (byte[] message : messages) {
            String trusttoken = "";
            try {
                trusttoken = do_RSADecryption(message, publicKeys.get(host));
            } catch (Exception ex) {
                System.out.println(ex);
                QLearn.updateQ(sender, verificator, false);
            }
            for (Map.Entry<Message, List<DTNHost>> entry : ack.entrySet()) {
                Message m = entry.getKey();
                List<DTNHost> hosts = entry.getValue();
                if (m.toString().equals(trusttoken)) {
                    if (hosts.contains(host)) {
                        Set<String> verificators;
                        Map<DTNHost, Set<String>> tup;
                        if (verificating.containsKey(m)) {
                            tup = verificating.get(m);
                            if (tup.containsKey(sender)) {
                                verificators = tup.get(sender);
                            } else {
                                verificators = new HashSet<String>();
                            }
                        } else {
                            tup = new HashMap<DTNHost, Set<String>>();
                            verificators = new HashSet<String>();
                        }

                        if (verificator.getVerificatorChecked().containsKey(sender)) {
                            if (verificator.getVerificatorChecked().get(sender).equals(trusttoken)) {
//                                System.out.println("skipped");
                                return;
                            }
                        }
                        String okay = "+" + verificator;
                        String fail = "-" + verificator;

                        if (!(verificators.contains(okay) || verificators.contains(fail))) {
//Default
                            if (sender != host) {
                                int rand = new Random().nextInt(2);
                                if (rand == 0) {
                                    verificators.add(okay);
                                }else{
                                    verificators.add(fail);
                                }
                            } else{
                                verificators.add(okay);
                            }
// end Default
//QLearn and Fuzzy
//                            if (sender != host) {
//                                QLearn.updateQ(sender, verificator, false);
//                            } else {
//                                if (!QLearn.suspended.contains(sender)) {
//                                    QLearn.updateQ(sender, verificator, true);
//                                }
//                            }
                            QLearn.updateIT(sender, verificator);

// QLearn
//                            if (QLearn.directTrust.get(verificator).get(sender) > 0.5 && !QLearn.getSuspended().contains(sender)) {
//                                verificators.add(okay);
//                            } else if (QLearn.directTrust.get(verificator).get(sender) < -0.5) {
//                                verificators.add(fail);
//                            }
// End Qlearn
// with Fuzzy
                            SimScenario.getInstance().getFb().setVariable("directTrust", QLearn.directTrust.get(verificator).get(sender));
                            SimScenario.getInstance().getFb().setVariable("indirectTrust", QLearn.getAvgIT(sender));
                            SimScenario.getInstance().getFb().setVariable("suspension", QLearn.suspension.get(sender));
                            SimScenario.getInstance().getFb().evaluate();
                            double trust = SimScenario.getInstance().getFb().getVariable("trust").getValue();
                            if (trust > 0.5 && !QLearn.getSuspended().contains(sender)) {
                                verificators.add(okay);
                            } else if (trust < -0.5) {
                                verificators.add(fail);
                            }
// end with Fuzzy
                            verificator.getVerificatorChecked().put(sender, trusttoken);
                        }
                        tup.put(sender, verificators);
                        verificating.put(m, tup);
                    } else {
                        System.out.println("Message : " + entry.getKey());
                        System.out.println("Host " + hosts);
                        System.out.println("detected  sender : " + sender + " host : " + host);
                        System.out.println("Message : " + m.toString());
                        System.out.println("Trusttoken : " + trusttoken);
                        System.out.println("Host : " + host);
                        QLearn.updateQ(sender, verificator, false);
                    }
                }
            }
        }

    }

    public static void createIncentive() {
        for (Map.Entry<Message, Map<DTNHost, Set<String>>> entry : verificating.entrySet()) {
            Message message = entry.getKey();
            Map<DTNHost, Set<String>> value1 = entry.getValue();

            for (Map.Entry<DTNHost, Set<String>> entry2 : value1.entrySet()) {
                DTNHost host = entry2.getKey();
                Set<String> verificators = entry2.getValue();

                int counterOk = 0;
                int counterFail = 0;

                for (String verificator : verificators) {
                    if (verificator.startsWith("+")) {

                        counterOk++;
                    }
                    if (verificator.startsWith("-")) {
                        counterFail++;
                    }
                }
                int totalVerificator = (int) Math.round(SimScenario.getInstance().getVerificator().size() / 2.0);
                if (counterOk >= totalVerificator || counterFail >= totalVerificator) {
                    //tambahi if iki
                    if (!finished.contains(message)) {
                        Set<String> hasil;

                        if (pending.containsKey(message)) {
                            hasil = pending.get(message);
                        } else {
                            hasil = new HashSet<String>();
                        }

                        String fail = "-" + host;
                        String ok = "+" + host;
//                        System.out.println(Math.round(SimScenario.getInstance().getVerificator().size()/2.0));
                        if (!(hasil.contains(ok) || hasil.contains(fail))) {
                            if (counterOk >= totalVerificator) {
                                hasil.add(ok);
                            }
                            if (counterFail >= totalVerificator) {
                                hasil.add(fail);
                                if (blacklistActive) {
                                    detectionTime.put(host, SimClock.getTime());
                                    blacklist.add(host);
                                }
                            }
                        }
                        pending.put(message, hasil);
                    }
                } else {
                    break;
                }
//                System.out.println("pending");
//                System.out.println(pending);
            }

        }

        if (!pending.isEmpty()) {
//            System.out.println("finished : " + finished);
            prosesPayment();
//            System.out.println("blacklist : " + blacklist);
        }
    }

    public static void prosesPayment() {
        //tambahi tobedel iki
        Set<Message> toBeDel = new HashSet<Message>();
        for (Map.Entry<Message, Set<String>> entry : pending.entrySet()) {
            Message m = entry.getKey();
            if (!(finished.contains(m))) {
                if (ack.get(m).size() == pending.get(m).size()) {
                    if (deposits.containsKey(m.toString())) {
                        Tuple<Transaction, Boolean> tup = deposits.get(m.toString());
                        float rewards = (float) m.getProperty("rewards");
                        if (!tup.getValue()) {
                            BlockChain.addTransaction(m.getFrom().getWallet().sendFunds(m.getTo().getWallet().publicKey, rewards));
                            Tuple<Transaction, Boolean> newTup = new Tuple<Transaction, Boolean>(tup.getKey(), true);
                            deposits.put(m.toString(), newTup);
                        }

                        List<DTNHost> hosts = ack.get(m);
                        List<DTNHost> pay = new ArrayList<DTNHost>();

                        for (DTNHost d : hosts) {
                            String ok = "+" + d;
                            if (!QLearn.suspended.contains(d)) {
                                if (pending.get(m).contains(ok)) {
                                    pay.add(d);
                                }
                            }
                        }

                        float amount = rewards / pay.size();

                        float updateamount = rewards;
                        int indx = 0;

                        for (DTNHost p : pay) {
                            if (indx < pay.size() - 1) {
                                BlockChain.addTransaction(m.getTo().getWallet().sendFunds(p.getWallet().publicKey, amount));
                                updateamount -= amount;
                            } else {
                                BlockChain.addTransaction(m.getTo().getWallet().sendFunds(p.getWallet().publicKey, updateamount));
                            }
                            indx++;
                        }
                        //tambahi tobedel iki
                        toBeDel.add(m);
                        finished.add(m);
                    }
                }
            }
        }

        //tambahi tobedel iki
        for (Message m : toBeDel) {
//            System.out.println("tobedel = " + m);
//            System.out.println("contains key " + pending.containsKey(m));
            pending.remove(m);
//            System.out.println("removed : " + ack.get(m));
            ack.remove(m);

        }
    }

    public static void setDeposit(String message, Transaction trx) {
        Tuple<Transaction, Boolean> tup = new Tuple<Transaction, Boolean>(trx, false);
        deposits.put(message, tup);
    }

    public static String do_RSADecryption(byte[] cipherText, PublicKey publicKey) throws Exception {
        Cipher cipher = Cipher.getInstance("RSA");

        cipher.init(Cipher.DECRYPT_MODE, publicKey);
        byte[] result = cipher.doFinal(cipherText);

        return new String(result);
    }

    public static Map<Message, List<DTNHost>> getAck() {
        return ack;
    }

    public static Set<DTNHost> getBlacklist() {
        return blacklist;
    }

    public static Map<DTNHost, Set<byte[]>> getTrustToken() {
        return trustToken;
    }

    public static Map<Message, Map<DTNHost, Set<String>>> getVerificating() {
        return verificating;
    }

    public static Map<Message, Set<String>> getPending() {
        return pending;
    }

    public static boolean isBlacklistActive() {
        return blacklistActive;
    }

    private static Double getAverage(ArrayList arr) {
        Double sum = 0.0;
        for (int i = 0; i < arr.size(); i++) {
            sum += (Double) arr.get(i);
        }
        return sum / arr.size();
    }

    public static Map<DTNHost, List<DTNHost>> getDetectedAccomplice() {
        return detectedAccomplice;
    }

}
