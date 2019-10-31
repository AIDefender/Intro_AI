import java.util.Random;

import core.ArcadeMachine;
import core.game.Game;
import tools.Recorder;


public class Train
{
    // ! 需要将weka.jar拷贝到/usr/lib/jvm/java-8-openjdk-amd64/jre/lib/ext/
    public static void main(String[] args) throws Exception
    {
        Recorder recorder  = new Recorder("AliensRecorder");
        
        Game.setRecorder(recorder);
        ArcadeMachine.playOneGame( "examples/gridphysics/aliens.txt", "examples/gridphysics/aliens_lvl0.txt", null, new Random().nextInt());
    }
}
