import java.util.ArrayList;
import java.util.Set;
import java.util.HashSet;

/* CompliantNode refers to a node that follows the rules (not malicious)*/
public class CompliantNode implements Node {
    private Set<Transaction> transactions; 
    private Set<Integer> followeeIds;    

    public CompliantNode(double p_graph, double p_malicious, double p_txDistribution, int numRounds) {
        // IMPLEMENT THIS
        this.transactions = new HashSet<Transaction>();
        this.followeeIds = new HashSet<Integer>();
    }

    public void setFollowees(boolean[] followees) {
        // IMPLEMENT THIS
        for (int i = 0; i < followees.length; i++) {
            if (followees[i]) {
                this.followeeIds.add(i);
            }
        }
    }

    public void setPendingTransaction(Set<Transaction> pendingTransactions) {
        // IMPLEMENT THIS
        this.transactions = pendingTransactions;
    }

    public Set<Transaction> sendToFollowers() {
       return this.transactions; 
    }

    public void receiveFromFollowees(Set<Candidate> candidates) {
        // IMPLEMENT THIS
        Set<Integer> n_followeeIds = new HashSet<>();
        for (Candidate candidate : candidates) {
            if (this.followeeIds.contains(candidate.sender)) {
                n_followeeIds.add(candidate.sender);
                this.transactions.add(candidate.tx);
            }
        }
        this.followeeIds = n_followeeIds;
    }
}
