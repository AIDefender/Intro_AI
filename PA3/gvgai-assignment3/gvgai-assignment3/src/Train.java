import java.util.Random;

import core.ArcadeMachine;
import core.game.Game;
import tools.Recorder;


public class Train
{
    // ! 需要将weka.jar拷贝到/usr/lib/jvm/java-8-openjdk-amd64/jre/lib/ext/
    // ! 运行weka方法:java -jar weka.jar
    public static void main(String[] args) throws Exception
    {
        Recorder recorder  = new Recorder("AliensRecorder");
        
        Game.setRecorder(recorder);
        ArcadeMachine.playOneGame( "examples/gridphysics/aliens.txt", "examples/gridphysics/aliens_lvl4.txt", null, new Random().nextInt());
    }
}
