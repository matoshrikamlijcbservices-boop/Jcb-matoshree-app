package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Agriculture
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Engineering
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MoneyOff
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.PostAdd
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Divider
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.credentials.CredentialManager
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.auth.AuthManager
import com.example.ui.screens.AllBillsScreen
import com.example.ui.screens.CustomersScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.ExpensesScreen
import com.example.ui.screens.ExportBackupScreen
import com.example.ui.screens.LedgerScreen
import com.example.ui.screens.MachinesScreen
import com.example.ui.screens.NewBillScreen
import com.example.ui.screens.OperatorsScreen
import com.example.ui.screens.PaymentsScreen
import com.example.ui.screens.ReportsScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.SignInScreen
import com.example.ui.screens.UserManagementScreen
import com.example.ui.screens.WorkRecordsScreen
import com.example.ui.theme.ConstructionYellow
import com.example.ui.theme.ConstructionYellowDark
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.NavyDark
import com.example.ui.viewmodel.MainViewModel
import com.example.ui.viewmodel.ScreenDestination
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import kotlinx.coroutines.launch

data class NavItem(
    val destination: ScreenDestination,
    val icon: ImageVector,
    val badgeCount: Int = 0
)

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MyApplicationTheme {
                val context = LocalContext.current
                val credentialManager = remember { CredentialManager.create(context) }
                val scope = rememberCoroutineScope()

                var currentUser by remember { mutableStateOf(Firebase.auth.currentUser) }

                DisposableEffect(Unit) {
                    val listener = FirebaseAuth.AuthStateListener { auth ->
                        currentUser = auth.currentUser
                    }
                    Firebase.auth.addAuthStateListener(listener)
                    onDispose {
                        Firebase.auth.removeAuthStateListener(listener)
                    }
                }

                // Attempt background auto sign-in
                LaunchedEffect(Unit) {
                    if (currentUser == null) {
                        AuthManager.attemptAutoSignIn(
                            context = context,
                            credentialManager = credentialManager,
                            onAuthSuccess = { currentUser = Firebase.auth.currentUser },
                            onUnauthenticated = {},
                            scope = this
                        )
                    }
                }

                if (currentUser == null) {
                    SignInScreen(
                        onAuthSuccess = {
                            currentUser = Firebase.auth.currentUser
                            viewModel.showMessage("Welcome, ${currentUser?.displayName ?: "User"}!")
                        }
                    )
                } else {
                    MainAppContent(
                        viewModel = viewModel,
                        userEmail = currentUser?.email ?: "Authorized User",
                        onSignOut = {
                            AuthManager.signOut(
                                context = context,
                                credentialManager = credentialManager,
                                onSignOutComplete = {
                                    currentUser = null
                                    viewModel.showMessage("Signed out successfully")
                                },
                                scope = scope
                            )
                        }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppContent(
    viewModel: MainViewModel,
    userEmail: String,
    onSignOut: () -> Unit
) {
    val currentScreen by viewModel.currentScreen.collectAsStateWithLifecycle()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.snackbarMessage.collect { msg ->
            snackbarHostState.showSnackbar(msg)
        }
    }

    BackHandler(enabled = currentScreen != ScreenDestination.DASHBOARD) {
        viewModel.navigateTo(ScreenDestination.DASHBOARD)
    }

    val allNavItems = listOf(
        NavItem(ScreenDestination.DASHBOARD, Icons.Default.Dashboard),
        NavItem(ScreenDestination.NEW_BILL, Icons.Default.PostAdd),
        NavItem(ScreenDestination.ALL_BILLS, Icons.Default.Receipt),
        NavItem(ScreenDestination.CUSTOMERS, Icons.Default.People),
        NavItem(ScreenDestination.MACHINES, Icons.Default.Agriculture),
        NavItem(ScreenDestination.WORK_RECORDS, Icons.Default.Schedule),
        NavItem(ScreenDestination.PAYMENTS, Icons.Default.AccountBalance),
        NavItem(ScreenDestination.EXPENSES, Icons.Default.MoneyOff),
        NavItem(ScreenDestination.OPERATORS, Icons.Default.Engineering),
        NavItem(ScreenDestination.REPORTS, Icons.Default.TrendingUp),
        NavItem(ScreenDestination.CUSTOMER_LEDGER, Icons.Default.Book),
        NavItem(ScreenDestination.SETTINGS, Icons.Default.Settings),
        NavItem(ScreenDestination.EXPORT_BACKUP, Icons.Default.CloudDownload),
        NavItem(ScreenDestination.USER_MANAGEMENT, Icons.Default.AdminPanelSettings)
    )

    val bottomBarItems = listOf(
        NavItem(ScreenDestination.DASHBOARD, Icons.Default.Dashboard),
        NavItem(ScreenDestination.NEW_BILL, Icons.Default.PostAdd),
        NavItem(ScreenDestination.ALL_BILLS, Icons.Default.Receipt),
        NavItem(ScreenDestination.PAYMENTS, Icons.Default.AccountBalance),
        NavItem(ScreenDestination.CUSTOMER_LEDGER, Icons.Default.Book)
    )

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                modifier = Modifier.width(310.dp),
                drawerContainerColor = NavyDark
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .background(ConstructionYellow, RoundedCornerShape(10.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Agriculture,
                                contentDescription = null,
                                tint = NavyDark,
                                modifier = Modifier.size(26.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Matoshree Kamalai",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                            Text(
                                text = "Land Developers & Builders",
                                color = ConstructionYellow,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = "Cloud Sync: $userEmail",
                        color = Color(0xFF94A3B8),
                        fontSize = 11.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Divider(color = Color(0xFF334155))

                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    items(allNavItems) { item ->
                        val isSelected = currentScreen == item.destination
                        NavigationDrawerItem(
                            icon = {
                                Icon(
                                    imageVector = item.icon,
                                    contentDescription = null,
                                    tint = if (isSelected) NavyDark else Color(0xFFCBD5E1)
                                )
                            },
                            label = {
                                Text(
                                    text = item.destination.title,
                                    color = if (isSelected) NavyDark else Color.White,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    fontSize = 13.sp
                                )
                            },
                            selected = isSelected,
                            onClick = {
                                viewModel.navigateTo(item.destination)
                                scope.launch { drawerState.close() }
                            },
                            colors = NavigationDrawerItemDefaults.colors(
                                selectedContainerColor = ConstructionYellow,
                                unselectedContainerColor = Color.Transparent
                            ),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .padding(vertical = 2.dp)
                                .testTag("nav_${item.destination.name.lowercase()}")
                        )
                    }
                }

                Divider(color = Color(0xFF334155))

                // Sign Out Action
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            scope.launch { drawerState.close() }
                            onSignOut()
                        }
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.ExitToApp,
                        contentDescription = "Sign Out",
                        tint = Color(0xFFEF4444)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Sign Out of Firebase",
                        color = Color(0xFFEF4444),
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp
                    )
                }
            }
        }
    ) {
        Scaffold(
            snackbarHost = { SnackbarHost(snackbarHostState) },
            topBar = {
                TopAppBar(
                    title = {
                        Column {
                            Text(
                                text = "Matoshree Kamalai Land Developers and Builders",
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = currentScreen.title,
                                fontSize = 12.sp,
                                color = ConstructionYellow,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    },
                    navigationIcon = {
                        IconButton(
                            onClick = { scope.launch { drawerState.open() } },
                            modifier = Modifier.testTag("btn_menu")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Menu,
                                contentDescription = "Menu",
                                tint = Color.White
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = NavyDark,
                        titleContentColor = Color.White,
                        navigationIconContentColor = Color.White
                    )
                )
            },
            bottomBar = {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 8.dp
                ) {
                    bottomBarItems.forEach { item ->
                        val isSelected = currentScreen == item.destination
                        NavigationBarItem(
                            selected = isSelected,
                            onClick = { viewModel.navigateTo(item.destination) },
                            icon = {
                                Icon(
                                    imageVector = item.icon,
                                    contentDescription = item.destination.title
                                )
                            },
                            label = {
                                Text(
                                    text = when (item.destination) {
                                        ScreenDestination.DASHBOARD -> "Home"
                                        ScreenDestination.NEW_BILL -> "New Bill"
                                        ScreenDestination.ALL_BILLS -> "Bills"
                                        ScreenDestination.PAYMENTS -> "Payments"
                                        ScreenDestination.CUSTOMER_LEDGER -> "Ledger"
                                        else -> item.destination.title
                                    },
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = NavyDark,
                                selectedTextColor = NavyDark,
                                indicatorColor = ConstructionYellow
                            )
                        )
                    }
                }
            }
        ) { innerPadding ->
            Surface(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                color = MaterialTheme.colorScheme.background
            ) {
                when (currentScreen) {
                    ScreenDestination.DASHBOARD -> DashboardScreen(viewModel)
                    ScreenDestination.NEW_BILL -> NewBillScreen(viewModel)
                    ScreenDestination.ALL_BILLS -> AllBillsScreen(viewModel)
                    ScreenDestination.CUSTOMERS -> CustomersScreen(viewModel)
                    ScreenDestination.MACHINES -> MachinesScreen(viewModel)
                    ScreenDestination.WORK_RECORDS -> WorkRecordsScreen(viewModel)
                    ScreenDestination.PAYMENTS -> PaymentsScreen(viewModel)
                    ScreenDestination.EXPENSES -> ExpensesScreen(viewModel)
                    ScreenDestination.OPERATORS -> OperatorsScreen(viewModel)
                    ScreenDestination.REPORTS -> ReportsScreen(viewModel)
                    ScreenDestination.CUSTOMER_LEDGER -> LedgerScreen(viewModel)
                    ScreenDestination.SETTINGS -> SettingsScreen(viewModel)
                    ScreenDestination.EXPORT_BACKUP -> ExportBackupScreen(viewModel)
                    ScreenDestination.USER_MANAGEMENT -> UserManagementScreen(viewModel)
                }
            }
        }
    }
}
