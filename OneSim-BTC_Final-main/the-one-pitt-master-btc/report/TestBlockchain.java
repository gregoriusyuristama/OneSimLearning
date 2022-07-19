/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package report;

/**
 *
 * @author gregoriusyuristamanugraha
 */
import btc.Block;
import btc.BlockChain;
import btc.Transaction;
import java.util.ArrayList;
public class TestBlockchain extends Report{

    public TestBlockchain() {
        init();
    }

    @Override
    protected void init() {
        super.init(); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void done() {
        write(BlockChain.blockchain.toString());
        for (Block bc : BlockChain.blockchain) {
            write(bc.previousHash);
            write(bc.hash);
            for (Transaction tc : bc.transactions) {
                out.println(tc.value);
            }
        }
        write(BlockChain.genesisTransaction.toString());
        super.done(); //To change body of generated methods, choose Tools | Templates.
    }
    
}
