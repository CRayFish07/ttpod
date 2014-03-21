/**
 * TODO Comment here.
 * date: 14-3-14 下午2:30
 * @author: yangyang.cong@ttpod.com
 */

Enumeration<NetworkInterface> netInterfaces  = NetworkInterface.getNetworkInterfaces();
String name = "eth0" ;
while (netInterfaces.hasMoreElements()) {
    NetworkInterface iface = netInterfaces.nextElement();
    // filters out 127.0.0.1 and inactive interfaces
    if (iface.isLoopback() || !iface.isUp())
        continue;

    if(name.equals(iface.getName())){
        Enumeration<InetAddress> ips = iface.getInetAddresses();
        def ipv4=[]
        while (ips.hasMoreElements()) {
            InetAddress addr = ips.nextElement();
            if (addr instanceof Inet6Address){//ignored IPV6
                continue;
            }
            ipv4 << addr
        }
        assert ipv4[0] == ipv4[1]
    }
}