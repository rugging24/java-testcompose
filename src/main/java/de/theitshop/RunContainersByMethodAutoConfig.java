package de.theitshop;

import org.junit.jupiter.api.extension.ExtendWith;

import java.lang.annotation.*;

@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@ExtendWith({RunContainersByMethod.class})
@Inherited
public @interface RunContainersByMethodAutoConfig { }
