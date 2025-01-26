package common;

import com.ecwid.consul.v1.ConsulClient;
import com.ecwid.consul.v1.agent.model.NewService;

public class ConsulService {

    private static final ConsulClient CONSUL_CLIENT = new ConsulClient("consul", 8500);

    public static void registerService(String serviceName, String serviceId,
                                       String serviceAddress, int port) {
        NewService newService = new NewService();
        newService.setName(serviceName);
        newService.setId(serviceId);
        newService.setPort(port);
        // Use Docker container name as the service address
        newService.setAddress(serviceAddress);

        // Register the service with Consul
        CONSUL_CLIENT.agentServiceRegister(newService);
        System.out.println("Service registered: " + serviceName);
    }

    public static String discoverServiceAddress(String serviceName) {
        var response = CONSUL_CLIENT.getHealthServices(serviceName, true, null);

        if (response.getValue().isEmpty()) {
            System.out.println("Service not found: " + serviceName);
            System.out.println("Available services: " + CONSUL_CLIENT.getAgentServices().getValue().keySet());
            throw new RuntimeException("Service not found: " + serviceName);
        }

        var service = response.getValue().get(0).getService(); // Get the first healthy instance

        System.out.println("Service found: " + serviceName + " at " + service.getAddress() + ":" + service.getPort());
        return service.getAddress() + ":" + service.getPort();
    }
}

