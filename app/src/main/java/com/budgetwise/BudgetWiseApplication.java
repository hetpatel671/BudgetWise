package com.budgetwise;

import android.app.Application;
import android.content.Context;
import com.budgetwise.data.repository.BudgetRepository;
import com.budgetwise.security.EncryptionManager;
import com.budgetwise.ai.EnhancedIntelligenceService;
import com.budgetwise.utils.ThemeManager;
import com.budgetwise.notifications.NotificationManager;

public class BudgetWiseApplication extends Application {
    private static BudgetWiseApplication instance;
    private BudgetRepository budgetRepository;
    private EncryptionManager encryptionManager;
    private EnhancedIntelligenceService intelligenceService;
    private ThemeManager themeManager;
    private NotificationManager notificationManager;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        
        // Set up global exception handler for better crash reporting
        Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(Thread thread, Throwable ex) {
                android.util.Log.e("BudgetWiseApp", "Uncaught exception", ex);
                // Let the system handle the crash
                System.exit(1);
            }
        });
        
        initializeServices();
    }

    private void initializeServices() {
        android.util.Log.d("BudgetWiseApp", "Starting service initialization");
        
        // Initialize core services with individual try-catch blocks
        try {
            encryptionManager = new EncryptionManager(this);
            android.util.Log.d("BudgetWiseApp", "EncryptionManager initialized");
        } catch (Exception e) {
            android.util.Log.e("BudgetWiseApp", "Error initializing EncryptionManager", e);
        }
        
        try {
            if (encryptionManager != null) {
                budgetRepository = new BudgetRepository(this, encryptionManager);
                android.util.Log.d("BudgetWiseApp", "BudgetRepository initialized");
            }
        } catch (Exception e) {
            android.util.Log.e("BudgetWiseApp", "Error initializing BudgetRepository", e);
        }
        
        try {
            themeManager = new ThemeManager(this);
            themeManager.setTheme(ThemeManager.THEME_DARK);
            themeManager.applyTheme();
            android.util.Log.d("BudgetWiseApp", "ThemeManager initialized");
        } catch (Exception e) {
            android.util.Log.e("BudgetWiseApp", "Error initializing ThemeManager", e);
        }
        
        try {
            notificationManager = new NotificationManager(this);
            android.util.Log.d("BudgetWiseApp", "NotificationManager initialized");
        } catch (Exception e) {
            android.util.Log.e("BudgetWiseApp", "Error initializing NotificationManager", e);
        }
        
        // Initialize AI service with delay and error handling
        android.os.Handler handler = new android.os.Handler(android.os.Looper.getMainLooper());
        handler.postDelayed(() -> {
            try {
                if (budgetRepository != null) {
                    intelligenceService = new EnhancedIntelligenceService(this, budgetRepository);
                    android.util.Log.d("BudgetWiseApp", "IntelligenceService initialized");
                }
            } catch (Exception e) {
                android.util.Log.e("BudgetWiseApp", "Error initializing IntelligenceService", e);
            }
        }, 2000); // 2 second delay
        
        // Schedule optional services with longer delays
        handler.postDelayed(() -> {
            try {
                if (notificationManager != null) {
                    notificationManager.scheduleBackupReminder();
                }
            } catch (Exception e) {
                android.util.Log.e("BudgetWiseApp", "Error scheduling backup reminder", e);
            }
        }, 5000); // 5 second delay
        
        handler.postDelayed(() -> {
            try {
                if (intelligenceService != null) {
                    intelligenceService.runCompleteAnalysis();
                }
            } catch (Exception e) {
                android.util.Log.e("BudgetWiseApp", "Error running AI analysis", e);
            }
        }, 10000); // 10 second delay
        
        android.util.Log.d("BudgetWiseApp", "Service initialization completed");
    }

    public static BudgetWiseApplication getInstance() {
        return instance;
    }

    public BudgetRepository getBudgetRepository() {
        if (budgetRepository == null) {
            android.util.Log.w("BudgetWiseApp", "BudgetRepository is null, creating fallback");
            try {
                if (encryptionManager == null) {
                    encryptionManager = new EncryptionManager(this);
                }
                budgetRepository = new BudgetRepository(this, encryptionManager);
            } catch (Exception e) {
                android.util.Log.e("BudgetWiseApp", "Error creating fallback BudgetRepository", e);
            }
        }
        return budgetRepository;
    }

    public EncryptionManager getEncryptionManager() {
        if (encryptionManager == null) {
            android.util.Log.w("BudgetWiseApp", "EncryptionManager is null, creating fallback");
            try {
                encryptionManager = new EncryptionManager(this);
            } catch (Exception e) {
                android.util.Log.e("BudgetWiseApp", "Error creating fallback EncryptionManager", e);
            }
        }
        return encryptionManager;
    }

    public EnhancedIntelligenceService getIntelligenceService() {
        if (intelligenceService == null) {
            android.util.Log.w("BudgetWiseApp", "IntelligenceService is null");
            // Don't create fallback for AI service as it's optional
        }
        return intelligenceService;
    }

    public ThemeManager getThemeManager() {
        if (themeManager == null) {
            android.util.Log.w("BudgetWiseApp", "ThemeManager is null, creating fallback");
            try {
                themeManager = new ThemeManager(this);
            } catch (Exception e) {
                android.util.Log.e("BudgetWiseApp", "Error creating fallback ThemeManager", e);
            }
        }
        return themeManager;
    }

    public NotificationManager getNotificationManager() {
        if (notificationManager == null) {
            android.util.Log.w("BudgetWiseApp", "NotificationManager is null, creating fallback");
            try {
                notificationManager = new NotificationManager(this);
            } catch (Exception e) {
                android.util.Log.e("BudgetWiseApp", "Error creating fallback NotificationManager", e);
            }
        }
        return notificationManager;
    }
}
