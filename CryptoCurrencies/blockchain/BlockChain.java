import java.util.*;
// Block Chain should maintain only limited block nodes to satisfy the functions
// You should not have all the blocks added to the block chain in memory 
// as it would cause a memory overflow.

/*
 * 
 * those blocks will form a tree structure with the genesis block at the root.
 * each block info {
 *  height: int
 *  hash: byte[]
 *  parentHash: byte[]
 *  transactions: Transaction[]
 *  utxoPool: UTXOPool
 * }
 */
public class BlockChain {
    public static final int CUT_OFF_AGE = 10;

    // map from txn hash to different data information about block 
    private Map<ByteArrayWrapper, Integer> blockHashtoHeight = new HashMap<>()  ;
    private Map<ByteArrayWrapper, Block> blockHashtoBlock = new HashMap<>();
    private Map<ByteArrayWrapper, TxHandler> blockHashtoTxHandler = new HashMap<>();

    // information about the max height block
    private Block maxHeightBlock;
    private TxHandler maxHeightTxHandler;
    private int maxHeight;
    
    // transaction pool 
    private TransactionPool transactionPool = new TransactionPool();

    /**
     * create an empty block chain with just a genesis block. Assume {@code genesisBlock} is a valid
     * block
     */
    public BlockChain(Block genesisBlock) {
        // IMPLEMENT THIS
        ByteArrayWrapper genesisBlockHash = new ByteArrayWrapper(genesisBlock.getHash());

        // initialize the block chain with the genesis block
        TxHandler genesisTxHandler = new TxHandler(new UTXOPool());
        this.blockHashtoHeight.put(genesisBlockHash, 0);
        this.blockHashtoBlock.put(genesisBlockHash, genesisBlock);
        this.blockHashtoTxHandler.put(genesisBlockHash, genesisTxHandler);
        this.handleCoinbaseTx(genesisBlock.getCoinbase(), genesisTxHandler);
   
        // initialize the max height block and tx handler
        this.maxHeightBlock = genesisBlock;
        this.maxHeightTxHandler = genesisTxHandler;
        this.maxHeight = 0;
    }

    /** Get the maximum height block */
    public Block getMaxHeightBlock() {
        // IMPLEMENT THIS
        return this.maxHeightBlock; 
    }

    /** Get the UTXOPool for mining a new block on top of max height block */
    public UTXOPool getMaxHeightUTXOPool() {
        // IMPLEMENT THIS
        return new UTXOPool(this.maxHeightTxHandler.getUTXOPool());
    }

    /** Get the transaction pool to mine a new block */
    public TransactionPool getTransactionPool() {
        // IMPLEMENT THIS
        return this.transactionPool;
    }

    /**
     * Add {@code block} to the block chain if it is valid. For validity, all transactions should be
     * valid and block should be at {@code height > (maxHeight - CUT_OFF_AGE)}.
     * 
     * <p>
     * For example, you can try creating a new block over the genesis block (block height 2) if the
     * block chain height is {@code <=
     * CUT_OFF_AGE + 1}. As soon as {@code height > CUT_OFF_AGE + 1}, you cannot create a new block
     * at height 2.
     * 
     * @return true if block is successfully added
     */
    public boolean addBlock(Block block) {
        // IMPLEMENT THIS

        // is genesis block 
        if (block.getPrevBlockHash() == null){
            return false; 
        }

        ByteArrayWrapper blockHash = new ByteArrayWrapper(block.getHash());
        ByteArrayWrapper prevBlockHash = new ByteArrayWrapper(block.getPrevBlockHash());
        if (!this.blockHashtoHeight.containsKey(prevBlockHash)){
            return false; 
        }

        // check if the height is greater than the max height - CUT_OFF_AGE
        int newHeight = this.blockHashtoHeight.get(prevBlockHash) + 1;
        if (newHeight <= this.maxHeight - CUT_OFF_AGE){
            return false; 
        }

        TxHandler newTxHandler = new TxHandler(new UTXOPool(this.blockHashtoTxHandler.get(prevBlockHash).getUTXOPool()));
        
        // handle transactions in the block
         Transaction[] validTxs = newTxHandler.handleTxs(
            block.getTransactions().toArray(new Transaction[0])
        );

        // there is some invalid transactions in the block itself 
        if (validTxs.length != block.getTransactions().size()){
            return false; 
        }

        // add handle coinbase tx after validation
        this.handleCoinbaseTx(block.getCoinbase(), newTxHandler);

        // add new block info to blockchain if all check passed 
        this.blockHashtoHeight.put(blockHash, newHeight);
        this.blockHashtoBlock.put(blockHash, block);
        this.blockHashtoTxHandler.put(blockHash, newTxHandler);

        // update new max height block information
        if (newHeight > this.maxHeight){
            this.maxHeight = newHeight;
            this.maxHeightBlock = block; 
            this.maxHeightTxHandler = newTxHandler;
        }        

        // remove transactions from transaction pool
        for (Transaction tx : validTxs){
            this.transactionPool.removeTransaction(tx.getHash());
        }
        
        return true; 
    }

    /** Add a transaction to the transaction pool */
    public void addTransaction(Transaction tx) {
        // IMPLEMENT THIS
        this.transactionPool.addTransaction(tx);
    }

    /**
     * add coinbase tx output to utxopool of current
     */
    private void handleCoinbaseTx(Transaction coinbase, TxHandler handler){
        for (int index = 0; index < coinbase.numOutputs(); index++){
            Transaction.Output output = coinbase.getOutput(index);
            handler.getUTXOPool().addUTXO(new UTXO(coinbase.getHash(), index), output);
        }
    }; 
}