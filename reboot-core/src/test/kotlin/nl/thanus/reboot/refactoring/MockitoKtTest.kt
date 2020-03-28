package nl.thanus.reboot.refactoring

import com.github.javaparser.StaticJavaParser
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration
import com.github.javaparser.resolution.declarations.ResolvedReferenceTypeDeclaration
import com.github.javaparser.symbolsolver.JavaSymbolSolver
import com.github.javaparser.symbolsolver.javaparsermodel.declarations.JavaParserClassDeclaration
import com.github.javaparser.symbolsolver.javaparsermodel.declarations.JavaParserInterfaceDeclaration
import com.github.javaparser.symbolsolver.resolution.typesolvers.MemoryTypeSolver
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

internal class MockitoKtTest : ReBootBase() {

    @Test
    fun `Rewrite @Mock including import`() {
        val code = """
            import org.mockito.Mock;
            
            class UsersControllerTest {
                @Mock
                private UsersService usersService;
            }
        """.trimIndent()
        val compilationUnit = StaticJavaParser.parse(code)

        rewriteMockitoFieldInjections(compilationUnit)

        val expectedCode = """
            import org.mockito.Mockito;
            
            class UsersControllerTest {
                private UsersService usersService = Mockito.mock(UsersService.class);
            }
        """.trimIndent()

        assertRefactored(compilationUnit, expectedCode)
    }

    @Test
    fun `Rewrite @Spy including import`() {
        val code = """
            import org.mockito.Spy;
            
            class UsersControllerTest {
                @Spy
                private UsersService usersService;
            }
        """.trimIndent()
        val compilationUnit = StaticJavaParser.parse(code)

        rewriteMockitoFieldInjections(compilationUnit)

        val expectedCode = """
            import org.mockito.Mockito;
            
            class UsersControllerTest {
                private UsersService usersService = Mockito.spy(new UsersService());
            }
        """.trimIndent()

        assertRefactored(compilationUnit, expectedCode)
    }

    @Nested
    @DisplayName("Rewrite @InjectMocks with object creation")
    inner class InjectMocks {

        @Nested
        @DisplayName("When resolved class has autowired fields")
        inner class AutowiredFields {

            @Test
            fun `Resolve constructor arguments`() {
                val typeDeclaration = mockk<JavaParserClassDeclaration>()
                configureTypeSolver(typeDeclaration)

                val compilationUnit = StaticJavaParser.parse(classWithInjectMocks())

                val clazz = """
                    public class UsersController {
                        @Autowired
                        private UsersService usersService;
                        @Autowired
                        private UsernameService usernameService;
                    }
                """.trimIndent()

                val classOrInterfaceDeclaration = StaticJavaParser.parseTypeDeclaration(clazz) as ClassOrInterfaceDeclaration

                every { typeDeclaration.isTypeParameter } returns false
                every { typeDeclaration.wrappedNode } returns classOrInterfaceDeclaration

                rewriteMockitoFieldInjections(compilationUnit)

                val expectedCode = """
                    import nl.thanus.demo.controllers.UsersController;
                    import org.mockito.Mockito;
                    
                    class UsersControllerTest {
                        private UsersService usersService = Mockito.mock(UsersService.class);
                        private UsernameService usernameService = Mockito.mock(UsernameService.class);
                        private UsersController usersController = new UsersController(usersService, usernameService);
                    }
                """.trimIndent()

                assertRefactored(compilationUnit, expectedCode)
            }

            @Test
            fun `Resolve constructor arguments only containing autowired`() {
                val typeDeclaration = mockk<JavaParserClassDeclaration>()
                configureTypeSolver(typeDeclaration)

                val compilationUnit = StaticJavaParser.parse(classWithInjectMocks())

                val clazz = """
                    public class UsersController {
                        @Autowired
                        private UsersService usersService;
                        @Autowired
                        private UsernameService usernameService;
                        private Object object;
                    }
                """.trimIndent()

                val classOrInterfaceDeclaration = StaticJavaParser.parseTypeDeclaration(clazz) as ClassOrInterfaceDeclaration

                every { typeDeclaration.isTypeParameter } returns false
                every { typeDeclaration.wrappedNode } returns classOrInterfaceDeclaration

                rewriteMockitoFieldInjections(compilationUnit)

                val expectedCode = """
                    import nl.thanus.demo.controllers.UsersController;
                    import org.mockito.Mockito;
                    
                    class UsersControllerTest {
                        private UsersService usersService = Mockito.mock(UsersService.class);
                        private UsernameService usernameService = Mockito.mock(UsernameService.class);
                        private UsersController usersController = new UsersController(usersService, usernameService);
                    }
                """.trimIndent()

                assertRefactored(compilationUnit, expectedCode)
            }
        }

        @Nested
        @DisplayName("When resolved class has RequiredArgsConstructor")
        inner class RequiredArgsConstructor {

            @Test
            fun `Resolve constructor arguments`() {
                val typeDeclaration = mockk<JavaParserClassDeclaration>()
                configureTypeSolver(typeDeclaration)

                val compilationUnit = StaticJavaParser.parse(classWithInjectMocks())

                val clazz = """
                    @RequiredArgsConstructor
                    public class UsersController {
                        private final UsersService usersService;
                        private final UsernameService usernameService;
                    }
                """.trimIndent()

                val classOrInterfaceDeclaration = StaticJavaParser.parseTypeDeclaration(clazz) as ClassOrInterfaceDeclaration

                every { typeDeclaration.isTypeParameter } returns false
                every { typeDeclaration.wrappedNode } returns classOrInterfaceDeclaration

                rewriteMockitoFieldInjections(compilationUnit)

                val expectedCode = """
                    import nl.thanus.demo.controllers.UsersController;
                    import org.mockito.Mockito;
                    
                    class UsersControllerTest {
                        private UsersService usersService = Mockito.mock(UsersService.class);
                        private UsernameService usernameService = Mockito.mock(UsernameService.class);
                        private UsersController usersController = new UsersController(usersService, usernameService);
                    }
                """.trimIndent()

                assertRefactored(compilationUnit, expectedCode)
            }

            @Test
            fun `Resolve constructor arguments without a non-required field`() {
                val typeDeclaration = mockk<JavaParserClassDeclaration>()
                configureTypeSolver(typeDeclaration)

                val compilationUnit = StaticJavaParser.parse(classWithInjectMocks())

                val clazz = """
                    @RequiredArgsConstructor
                    public class UsersController {
                        private final UsersService usersService;
                        private final UsernameService usernameService;
                        private Object object;
                    }
                """.trimIndent()

                val classOrInterfaceDeclaration = StaticJavaParser.parseTypeDeclaration(clazz) as ClassOrInterfaceDeclaration

                every { typeDeclaration.isTypeParameter } returns false
                every { typeDeclaration.wrappedNode } returns classOrInterfaceDeclaration

                rewriteMockitoFieldInjections(compilationUnit)

                val expectedCode = """
                    import nl.thanus.demo.controllers.UsersController;
                    import org.mockito.Mockito;
                    
                    class UsersControllerTest {
                        private UsersService usersService = Mockito.mock(UsersService.class);
                        private UsernameService usernameService = Mockito.mock(UsernameService.class);
                        private UsersController usersController = new UsersController(usersService, usernameService);
                    }
                """.trimIndent()

                assertRefactored(compilationUnit, expectedCode)
            }
        }

        @Nested
        @DisplayName("When resolved class has AllArgsConstructor")
        inner class AllArgsConstructor {

            @Test
            fun `Resolve constructor arguments`() {
                val typeDeclaration = mockk<JavaParserClassDeclaration>()
                configureTypeSolver(typeDeclaration)

                val compilationUnit = StaticJavaParser.parse(classWithInjectMocks())

                val clazz = """
                    @AllArgsConstructor
                    public class UsersController {
                        private final UsersService usersService;
                        private final UsernameService usernameService;
                    }
                """.trimIndent()

                val classOrInterfaceDeclaration = StaticJavaParser.parseTypeDeclaration(clazz) as ClassOrInterfaceDeclaration

                every { typeDeclaration.isTypeParameter } returns false
                every { typeDeclaration.wrappedNode } returns classOrInterfaceDeclaration

                rewriteMockitoFieldInjections(compilationUnit)

                val expectedCode = """
                    import nl.thanus.demo.controllers.UsersController;
                    import org.mockito.Mockito;
                    
                    class UsersControllerTest {
                        private UsersService usersService = Mockito.mock(UsersService.class);
                        private UsernameService usernameService = Mockito.mock(UsernameService.class);
                        private UsersController usersController = new UsersController(usersService, usernameService);
                    }
                """.trimIndent()

                assertRefactored(compilationUnit, expectedCode)
            }

            @Test
            fun `Resolve constructor arguments when fields are not final`() {
                val typeDeclaration = mockk<JavaParserClassDeclaration>()
                configureTypeSolver(typeDeclaration)

                val compilationUnit = StaticJavaParser.parse(classWithInjectMocks())

                val clazz = """
                    @AllArgsConstructor
                    public class UsersController {
                        private UsersService usersService;
                        private UsernameService usernameService;
                    }
                """.trimIndent()

                val classOrInterfaceDeclaration = StaticJavaParser.parseTypeDeclaration(clazz) as ClassOrInterfaceDeclaration

                every { typeDeclaration.isTypeParameter } returns false
                every { typeDeclaration.wrappedNode } returns classOrInterfaceDeclaration

                rewriteMockitoFieldInjections(compilationUnit)

                val expectedCode = """
                    import nl.thanus.demo.controllers.UsersController;
                    import org.mockito.Mockito;
                    
                    class UsersControllerTest {
                        private UsersService usersService = Mockito.mock(UsersService.class);
                        private UsernameService usernameService = Mockito.mock(UsernameService.class);
                        private UsersController usersController = new UsersController(usersService, usernameService);
                    }
                """.trimIndent()

                assertRefactored(compilationUnit, expectedCode)
            }

            @Test
            fun `Resolve constructor arguments without a static initialized field`() {
                val typeDeclaration = mockk<JavaParserClassDeclaration>()
                configureTypeSolver(typeDeclaration)

                val compilationUnit = StaticJavaParser.parse(classWithInjectMocks())

                val clazz = """
                    @AllArgsConstructor
                    public class UsersController {
                        private final UsersService usersService;
                        private final UsernameService usernameService;
                        public static int COUNTER = 1;
                    }
                """.trimIndent()

                val classOrInterfaceDeclaration = StaticJavaParser.parseTypeDeclaration(clazz) as ClassOrInterfaceDeclaration

                every { typeDeclaration.isTypeParameter } returns false
                every { typeDeclaration.wrappedNode } returns classOrInterfaceDeclaration

                rewriteMockitoFieldInjections(compilationUnit)

                val expectedCode = """
                    import nl.thanus.demo.controllers.UsersController;
                    import org.mockito.Mockito;
                    
                    class UsersControllerTest {
                        private UsersService usersService = Mockito.mock(UsersService.class);
                        private UsernameService usernameService = Mockito.mock(UsernameService.class);
                        private UsersController usersController = new UsersController(usersService, usernameService);
                    }
                """.trimIndent()

                assertRefactored(compilationUnit, expectedCode)
            }
        }

        @Nested
        @DisplayName("When resolved class has a constructor")
        inner class Constructor {

            @Test
            fun `Resolve constructor arguments when single constructor`() {
                val typeDeclaration = mockk<JavaParserClassDeclaration>()
                configureTypeSolver(typeDeclaration)

                val compilationUnit = StaticJavaParser.parse(classWithInjectMocks())

                val clazz = """
                    public class UsersController {
                        private final UsersService usersService;
                        private final UsernameService usernameService;

                        public UsersController(UsersService usersService, UsernameService usernameService) {
                            this.usersService = usersService;
                            this.usernameService = usernameService;
                        }
                    }
                """.trimIndent()

                val classOrInterfaceDeclaration = StaticJavaParser.parseTypeDeclaration(clazz) as ClassOrInterfaceDeclaration

                every { typeDeclaration.isTypeParameter } returns false
                every { typeDeclaration.wrappedNode } returns classOrInterfaceDeclaration

                rewriteMockitoFieldInjections(compilationUnit)

                val expectedCode = """
                    import nl.thanus.demo.controllers.UsersController;
                    import org.mockito.Mockito;
                    
                    class UsersControllerTest {
                        private UsersService usersService = Mockito.mock(UsersService.class);
                        private UsernameService usernameService = Mockito.mock(UsernameService.class);
                        private UsersController usersController = new UsersController(usersService, usernameService);
                    }
                """.trimIndent()

                assertRefactored(compilationUnit, expectedCode)
            }

            @Test
            fun `Resolve constructor arguments when multiple constructors (single autowired)`() {
                val typeDeclaration = mockk<JavaParserClassDeclaration>()
                configureTypeSolver(typeDeclaration)

                val compilationUnit = StaticJavaParser.parse(classWithInjectMocks())

                val clazz = """
                    public class UsersController {
                        private final UsersService usersService;
                        private final UsernameService usernameService;

                        @Autowired
                        public UsersController(UsersService usersService, UsernameService usernameService) {
                            this.usersService = usersService;
                            this.usernameService = usernameService;
                        }

                        public UsersController() {
                            this.usersService = null;
                            this.usernameService = null;
                        }
                    }
                """.trimIndent()

                val classOrInterfaceDeclaration = StaticJavaParser.parseTypeDeclaration(clazz) as ClassOrInterfaceDeclaration

                every { typeDeclaration.isTypeParameter } returns false
                every { typeDeclaration.wrappedNode } returns classOrInterfaceDeclaration

                rewriteMockitoFieldInjections(compilationUnit)

                val expectedCode = """
                    import nl.thanus.demo.controllers.UsersController;
                    import org.mockito.Mockito;
                    
                    class UsersControllerTest {
                        private UsersService usersService = Mockito.mock(UsersService.class);
                        private UsernameService usernameService = Mockito.mock(UsernameService.class);
                        private UsersController usersController = new UsersController(usersService, usernameService);
                    }
                """.trimIndent()

                assertRefactored(compilationUnit, expectedCode)
            }
        }

        @Test
        fun `Object creation should not contain any constructor arguments when resolving type is not a class`() {
            val typeDeclaration = mockk<JavaParserInterfaceDeclaration>()
            configureTypeSolver(typeDeclaration)

            val compilationUnit = StaticJavaParser.parse(classWithInjectMocks())

            every { typeDeclaration.isTypeParameter } returns false

            rewriteMockitoFieldInjections(compilationUnit)

            val expectedCode = """
                import nl.thanus.demo.controllers.UsersController;
                import org.mockito.Mockito;
                
                class UsersControllerTest {
                    private UsersService usersService = Mockito.mock(UsersService.class);
                    private UsernameService usernameService = Mockito.mock(UsernameService.class);
                    private UsersController usersController = new UsersController();
                }
            """.trimIndent()

            assertRefactored(compilationUnit, expectedCode)
        }

        private fun classWithInjectMocks() = """
                        import org.mockito.InjectMocks;
                        import nl.thanus.demo.controllers.UsersController;
                        
                        class UsersControllerTest {
                            @Mock
                            private UsersService usersService;
                            @Mock
                            private UsernameService usernameService;
                            @InjectMocks
                            private UsersController usersController;
                        }
                    """.trimIndent()

        @Test
        fun `Use NullExpression as constructor argument when it cannot find matching field`() {
            val code = """
                import org.mockito.InjectMocks;
                import nl.thanus.demo.controllers.UsersController;
                
                class UsersControllerTest {
                    @Mock
                    private UsersService usersService;
                    @InjectMocks
                    private UsersController usersController;
                }
            """.trimIndent()

            val typeDeclaration = mockk<JavaParserClassDeclaration>()
            configureTypeSolver(typeDeclaration)

            val compilationUnit = StaticJavaParser.parse(code)

            val clazz = """
                @RequiredArgsConstructor
                public class UsersController {
                    private final UsersService usersService;
                    private final UsernameService usernameService;
                }
            """.trimIndent()

            val classOrInterfaceDeclaration = StaticJavaParser.parseTypeDeclaration(clazz) as ClassOrInterfaceDeclaration

            every { typeDeclaration.isTypeParameter } returns false
            every { typeDeclaration.wrappedNode } returns classOrInterfaceDeclaration

            rewriteMockitoFieldInjections(compilationUnit)

            val expectedCode = """
                import nl.thanus.demo.controllers.UsersController;
                import org.mockito.Mockito;
                
                class UsersControllerTest {
                    private UsersService usersService = Mockito.mock(UsersService.class);
                    private UsersController usersController = new UsersController(usersService, null);
                }
            """.trimIndent()

            assertRefactored(compilationUnit, expectedCode)
        }

        @Test
        fun `Object creation should not contain any argument when resolving class has no fields`() {
            val code = """
                import org.mockito.InjectMocks;
                import nl.thanus.demo.controllers.UsersController;
                
                class UsersControllerTest {
                    @InjectMocks
                    private UsersController usersController;
                }
            """.trimIndent()

            val typeDeclaration = mockk<JavaParserClassDeclaration>()
            configureTypeSolver(typeDeclaration)

            val compilationUnit = StaticJavaParser.parse(code)

            val clazz = """
                public class UsersController {
                }
            """.trimIndent()

            val classOrInterfaceDeclaration = StaticJavaParser.parseTypeDeclaration(clazz) as ClassOrInterfaceDeclaration

            every { typeDeclaration.isTypeParameter } returns false
            every { typeDeclaration.wrappedNode } returns classOrInterfaceDeclaration

            rewriteMockitoFieldInjections(compilationUnit)

            val expectedCode = """
                import nl.thanus.demo.controllers.UsersController;
                
                class UsersControllerTest {
                    private UsersController usersController = new UsersController();
                }
            """.trimIndent()

            assertRefactored(compilationUnit, expectedCode)
        }

        private fun configureTypeSolver(typeDeclaration: ResolvedReferenceTypeDeclaration) {
            val memoryTypeSolver = MemoryTypeSolver()
            memoryTypeSolver.addDeclaration("nl.thanus.demo.controllers.UsersController", typeDeclaration)
            StaticJavaParser.getConfiguration().setSymbolResolver(JavaSymbolSolver(memoryTypeSolver))
        }

        @Test
        fun `Object creation should not contain any constructor arguments when resolving arguments fails`() {
            val code = """
                import org.mockito.InjectMocks;
                
                class UsersControllerTest {
                    @InjectMocks
                    private UsersController usersController;
                }
            """.trimIndent()

            val memoryTypeSolver = MemoryTypeSolver()
            StaticJavaParser.getConfiguration().setSymbolResolver(JavaSymbolSolver(memoryTypeSolver))

            val compilationUnit = StaticJavaParser.parse(code)

            rewriteMockitoFieldInjections(compilationUnit)

            val expectedCode = """
                class UsersControllerTest {
                    private UsersController usersController = new UsersController();
                }
            """.trimIndent()

            assertRefactored(compilationUnit, expectedCode)
        }
    }

    @Test
    fun `Should not add Mockito import when there is already one`() {
        val code = """
            import org.mockito.Mock;
            import org.mockito.Mockito;
            
            class UsersControllerTest {
                @Mock
                private UsersService usersService;
            }
        """.trimIndent()
        val compilationUnit = StaticJavaParser.parse(code)

        rewriteMockitoFieldInjections(compilationUnit)

        val expectedCode = """
            import org.mockito.Mockito;
            
            class UsersControllerTest {
                private UsersService usersService = Mockito.mock(UsersService.class);
            }
        """.trimIndent()

        assertRefactored(compilationUnit, expectedCode)
    }
}
