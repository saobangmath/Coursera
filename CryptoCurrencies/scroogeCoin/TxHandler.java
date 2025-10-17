import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class TxHandler {
    private UTXOPool utxoPool;
    /**
     * Creates a public ledger whose current UTXOPool (collection of unspent transaction outputs) is
     * {@code utxoPool}. This should make a copy of utxoPool by using the UTXOPool(UTXOPool uPool)
     * constructor.
     */
    public TxHandler(UTXOPool utxoPool) {
        // IMPLEMENT THIS
        this.utxoPool = new UTXOPool(utxoPool);
    }

    /**
     * @return true if:
     * (1) all outputs claimed by {@code tx} are in the current UTXO pool, 
     * (2) the signatures on each input of {@code tx} are valid, 
     * (3) no UTXO is claimed multiple times by {@code tx},
     * (4) all of {@code tx}s output values are non-negative, and
     * (5) the sum of {@code tx}s input values is greater than or equal to the sum of its output
     *     values; and false otherwise.
     */
    public boolean isValidTx(Transaction tx) {
        // IMPLEMENT THIS
        Set<UTXO> usedUTXOs = new HashSet<>(); // make sure that there are no duplicate UTXOs
        
        double inputValueSum = 0, outputValueSum = 0;

        for (int inputIndex = 0; inputIndex < tx.numInputs(); inputIndex++){
            Transaction.Input input = tx.getInput(inputIndex);
            UTXO utxo = new UTXO(input.prevTxHash, input.outputIndex);
            
            // condition 1, 3
            if (!this.utxoPool.contains(utxo) || usedUTXOs.contains(utxo)) {
                return false;
            }

            Transaction.Output output = this.utxoPool.getTxOutput(utxo); 
            // condition 2
            if (!Crypto.verifySignature(
                output.address, 
                tx.getRawDataToSign(inputIndex), 
                input.signature)
            ) {
                return false;
            }

            inputValueSum += output.value;
            usedUTXOs.add(utxo); 
        }

        for (Transaction.Output output : tx.getOutputs()){
            // condition 4
            if (output.value < 0) {
                return false;
            }

            outputValueSum += output.value;
        }

        // condition 5
        if (inputValueSum < outputValueSum) {
            return false;
        }
        return true; 
    }

    /**
     * Handles each epoch by receiving an unordered array of proposed transactions, checking each
     * transaction for correctness, returning a mutually valid array of accepted transactions, and
     * updating the current UTXO pool as appropriate.
     */
    public Transaction[] handleTxs(Transaction[] possibleTxs) {
        // IMPLEMENT THIS
        List<Transaction> validTxs = new ArrayList<>();
        for (Transaction tx : possibleTxs){
            if (isValidTx(tx)) {
                validTxs.add(tx);
                this.addValidTx(tx);
            }
        }
        return validTxs.toArray(new Transaction[validTxs.size()]);
    }

    private void addValidTx(Transaction tx) {
        for (int inputIndex = 0; inputIndex < tx.numInputs(); inputIndex++){
            Transaction.Input input = tx.getInput(inputIndex);
            UTXO utxo = new UTXO(input.prevTxHash, input.outputIndex);
            this.utxoPool.removeUTXO(utxo);
        }

        for (int outputIndex = 0; outputIndex < tx.numOutputs(); outputIndex++){
            Transaction.Output output = tx.getOutput(outputIndex); 
            this.utxoPool.addUTXO(new UTXO(tx.getHash(), outputIndex), output);
        }
    }
}
