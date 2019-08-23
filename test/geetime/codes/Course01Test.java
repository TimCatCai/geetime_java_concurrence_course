package geetime.codes;

import org.junit.jupiter.api.Test;

import java.util.logging.Level;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.*;

class Course01Test {
    private Logger logger = Logger.getLogger(this.getClass().getSimpleName());
    @Test
    void calc() {
        logger.log(Level.INFO, "final result: " + Course01.calc());
    }
}