package common;

import com.ecwid.consul.v1.ConsulClient;
import com.ecwid.consul.v1.agent.model.NewService;
import com.ecwid.consul.v1.agent.model.Service;
import org.checkerframework.checker.nullness.qual.Nullable;

public class ConsulService {

    private static final ConsulClient CONSUL_CLIENT = new ConsulClient("consul", 8500);

    public static void registerService(String serviceName, String serviceId, int port) {
        NewService newService = new NewService();
        newService.setName(serviceName);
        newService.setId(serviceId);
        newService.setPort(port);

        // Register the service with Consul
        CONSUL_CLIENT.agentServiceRegister(newService);
        System.out.println("Service registered: " + serviceName);
    }

    @Nullable
    public static String discoverServiceAddress(String serviceName) {
        Service service = CONSUL_CLIENT.getAgentServices().getValue()
                .get(serviceName);

        if (service == null) {
            System.out.println("Service not found: " + serviceName);
            return null;
        } else {
            System.out.println("Service found: " + serviceName);
            return service.getAddress() + ":" + service.getPort();
        }
    }
}

