package cs451.tools;

import cs451.Host;

import java.util.List;

public class Utils {
    public static Host getHostFromId(List<Host> hosts, int id) {
        return hosts.get(id-1);
    }
}
