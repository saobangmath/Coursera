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
        this.blockHashtoHeight.put(genesisBlockHash, 0);
        this.blockHashtoBlock.put(genesisBlockHash, genesisBlock);
        this.blockHashtoTxHandler.put(genesisBlockHash, new TxHandler(new UTXOPool()));
    
        this.maxHeightBlock = genesisBlock;
        this.maxHeightTxHandler = this.blockHashtoTxHandler.get(genesisBlockHash);
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
        return this.maxHeightTxHandler.getUTXOPool();
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

        if (!this.blockHashtoHeight.containsKey(prevBlockHash) || 
             this.blockHashtoHeight.containsKey(blockHash)){
            return false; 
        }

        // check if the height is greater than the max height - CUT_OFF_AGE
        int newHeight = this.blockHashtoHeight.get(prevBlockHash) + 1;
        if (newHeight <= this.maxHeight - CUT_OFF_AGE){
            return false; 
        }

        TxHandler newTxHandler = new TxHandler(new UTXOPool(this.blockHashtoTxHandler.get(prevBlockHash).getUTXOPool()));

        // add new block info to blockchain 
        this.blockHashtoHeight.put(blockHash, newHeight);
        this.blockHashtoBlock.put(blockHash, block);
        this.blockHashtoTxHandler.put(blockHash, newTxHandler);

        // update new max height block information
        if (newHeight > this.maxHeight){
            this.maxHeight = newHeight;
        }        

        if (this.maxHeight == newHeight){
            this.maxHeightBlock = block; 
            this.maxHeightTxHandler = newTxHandler;
        }

        // handle transactions in the block
        Transaction[] validTxs = newTxHandler.handleTxs(
            block.getTransactions().toArray(new Transaction[0])
        );

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
}