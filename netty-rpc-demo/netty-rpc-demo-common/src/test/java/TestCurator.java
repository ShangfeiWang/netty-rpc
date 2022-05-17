import com.wsf.netty.rpc.common.config.zookeeper.CuratorClient;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import java.nio.charset.StandardCharsets;

/**
 * @author wsf
 * @since 20220526
 */
@Slf4j
public class TestCurator {

    @Test
    public void test1() throws Exception {
        CuratorClient curatorClient = new CuratorClient("124.223.109.220:2181");

        // 创建瞬时节点
        curatorClient.createPathNodeData("/wsf/test1", "hello".getBytes(StandardCharsets.UTF_8));
        // 创建持久节点
        curatorClient.createPersistentPathNodeData("/wsf/test2", "hello".getBytes(StandardCharsets.UTF_8));

        curatorClient.close();
    }

    @Test
    public void testDeletePath() throws Exception {
        CuratorClient curatorClient = new CuratorClient("127.0.0.1:2181");
        curatorClient.deletePath("/wsf/test2");
    }

    @Test
    public void testWatchNode() throws Exception {
        String path = "/wsf/test2";
        CuratorClient curatorClient = new CuratorClient("127.0.0.1:2181");
        curatorClient.createPersistentPathNodeData(path, "hello".getBytes(StandardCharsets.UTF_8));
        byte[] nodeData = curatorClient.getNodeData(path);
        String value = new String(nodeData);
        System.out.println("value:" + value);
        curatorClient.deletePath(path);
    }

}
