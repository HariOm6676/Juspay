package JuspayHackathonPartB;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Random;
import java.util.Scanner;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

class TreeNode {
    String name;
    int lockBy;
    boolean isLoc;
    List<TreeNode> childs;
    TreeNode parent;
    Set<TreeNode> locDesc;
    AtomicInteger version; // For optimistic locking

    TreeNode(String nm, TreeNode par) {
        name = nm;
        lockBy = -1;
        parent = par;
        isLoc = false;
        childs = new ArrayList<>();
        locDesc = new HashSet<>();
        version = new AtomicInteger(0); // Initialize version to 0
    }

    void addChild(List<String> a) {
        for (String nm : a) {
            childs.add(new TreeNode(nm, this));
        }
    }
}

class MTree {
    TreeNode root;
    Map<String, TreeNode> nttm;

    MTree(String name) {
        root = new TreeNode(name, null);
        nttm = new HashMap<>();
    }

    void makeMTree(List<String> a, int m) {
        Queue<TreeNode> q = new LinkedList<>();
        q.add(root);
        int k = 1, i, n = a.size();
        while (!q.isEmpty()) {
            TreeNode r = q.poll();
            nttm.put(r.name, r);
            List<String> b = new ArrayList<>();
            for (i = k; i < Math.min(n, m + n); i++) {
                b.add(a.get(i));
            }
            r.addChild(b);
            for (TreeNode child : r.childs) {
                q.add(child);
            }
            k = i;
        }
    }

    boolean lock(String name, int id) {
        TreeNode r = nttm.get(name);
        if (r == null) {
            return false;  // Node not found
        }

        int currentVersion = r.version.get();  // Get the current version

        // Check conditions for locking (descendants and ancestors must be unlocked)
        if (r.isLoc || !r.locDesc.isEmpty()) {
            return false;  // Node already locked or has locked descendants
        }
        TreeNode par = r.parent;
        while (par != null) {
            if (par.isLoc) {
                return false;  // Ancestor is+ locked, cannot lock this node
            }
            par = par.parent;
        }

        // Try locking the node
        if (r.version.compareAndSet(currentVersion, currentVersion + 1)) {
            // Lock the node and update its state
            r.isLoc = true;
            r.lockBy = id;
            updateParents(r.parent, r);  // Update ancestors' lock state
            return true;
        } else {
            return false;  // Fail fast on version mismatch
        }
    }

    boolean unlock(String name, int id) {
        TreeNode r = nttm.get(name);
        if (r == null) {
            return false;  // Node doesn't exist
        }

        int currentVersion = r.version.get();  // Get current version

        // Check conditions for unlocking
        if (!r.isLoc || r.lockBy != id) {
            return false;  // Either not locked or locked by a different ID
        }

        // Attempt to update the version to unlock the node
        if (r.version.compareAndSet(currentVersion, currentVersion + 1)) {
            // Update node state after successful version update
            r.isLoc = false;
            r.lockBy = -1;

            // Update ancestor nodes' locked descendants information
            TreeNode par = r.parent;
            while (par != null) {
                par.locDesc.remove(r);  // Remove the unlocked node from each ancestor's locked descendants set
                par = par.parent;  // Move up to the next ancestor
            }
            return true;  // Unlock successful
        } else {
            return false;  // Version mismatch, unlock failed
        }
    }

    boolean upgrade(String name, int id) {
        TreeNode r = nttm.get(name);

        // Early exit conditions: node not found, already locked, or no locked descendants
        if (r == null || r.isLoc || r.locDesc.isEmpty()) {
            return false;
        }

        int currentVersion = r.version.get();  // Get current version

        // Check conditions for upgrading
        for (TreeNode ld : r.locDesc) {
            if (ld.lockBy != id) {
                return false;  // One of the locked descendants is not locked by this ID
            }
        }

        // Attempt to update the version to upgrade the node
        if (r.version.compareAndSet(currentVersion, currentVersion + 1)) {
            // Unlock descendants after a successful version update
            Set<TreeNode> stt = new HashSet<>(r.locDesc);  // Make a copy of locDesc
            for (TreeNode ld : stt) {
                unlock(ld.name, id);  // Unlock each descendant
            }

            // Lock the current node
            return lock(name, id);  // Lock the current node for this ID
        } else {
            // Version mismatch, upgrade failed
            return false;
        }
    }

    void updateParents(TreeNode r, TreeNode curr) {
        while (r != null) {
            r.locDesc.add(curr);
            r = r.parent;
        }
    }
}

public class Approach5 {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        int n = sc.nextInt();  // Number of nodes
        int m = sc.nextInt();  // Maximum number of children for each node
        int q = sc.nextInt();  // Number of operations
        List<String> a = new ArrayList<>(n);
        for (int i = 0; i < n; i++) {
            a.add(sc.next());
        }
        MTree tree = new MTree(a.get(0));  // Create tree with root
        tree.makeMTree(a, m);  // Build the tree

        // Create a thread pool with 4 threads
        ExecutorService executor = Executors.newFixedThreadPool(4);

        Runnable task = () -> {
            Random rand = new Random();
            for (int i = 0; i < q; i++) {
                int ot = rand.nextInt(3) + 1;  // Random operation: 1 for lock, 2 for unlock, 3 for upgrade
                String name = a.get(rand.nextInt(n));  // Random node name
                int id = rand.nextInt(3);  // Random ID
                boolean result = false;
                switch (ot) {
                    case 1:
                        result = tree.lock(name, id);  // Lock operation
                        break;
                    case 2:
                        result = tree.unlock(name, id);  // Unlock operation
                        break;
                    case 3:
                        result = tree.upgrade(name, id);  // Upgrade operation
                        break;
                }
                System.out.println("Thread " + Thread.currentThread().getName() +
                    " Operation: " + ot + " Node: " + name + " ID: " + id + " Result: " + result);
                try {
                    Thread.sleep(500);  // Sleep for 500ms between operations
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        };

        // Submit multiple tasks to the thread pool
        for (int i = 0; i < 4; i++) {
            executor.submit(task);
            try {
                Thread.sleep(500);  // Sleep for 500ms between task submissions
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        // Shut down the executor service
        executor.shutdown();
        sc.close();
    }
}
