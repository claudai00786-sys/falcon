package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ProductEntity
import com.example.ui.components.AppBrandLogo
import com.example.ui.components.formatCurrency
import com.example.ui.theme.*
import com.example.ui.util.AppStrings
import com.example.viewmodel.CartItem
import com.example.viewmodel.PosViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CatalogScreen(
    viewModel: PosViewModel,
    modifier: Modifier = Modifier
) {
    val currentLanguage by viewModel.currentLanguage.collectAsState()
    val products by viewModel.products.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val cart by viewModel.cart.collectAsState()
    val factories by viewModel.factories.collectAsState()
    val selectedFactory by viewModel.selectedOrderFactory.collectAsState()
    val orderDate by viewModel.orderDate.collectAsState()

    var showCartSheet by remember { mutableStateOf(false) }
    var showAddProductDialog by remember { mutableStateOf(false) }
    var editingProduct by remember { mutableStateOf<ProductEntity?>(null) }

    val categories = listOf(
        "American Pedestal Fan",
        "American Exhaust Fan",
        "American Universal Fan",
        "Bracket Pedestal Fan",
        "Bracket Exhaust Fan",
        "Bracket Universal Fan"
    )

    val filteredProducts = remember(products, selectedCategory, searchQuery) {
        products.filter { p ->
            val matchesCategory = p.cat == selectedCategory
            val matchesQuery = searchQuery.isBlank() || p.name.contains(searchQuery, ignoreCase = true)
            matchesCategory && matchesQuery
        }
    }

    val isUr = currentLanguage == "ur"
    val cartTotalCount = cart.sumOf { it.quantity }
    val cartTotalAmount = cart.sumOf { it.lineTotal }

    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 14.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // Search and Add Product Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { viewModel.setSearchQuery(it) },
                    placeholder = { Text(AppStrings.searchCatalog(isUr)) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { viewModel.setSearchQuery("") }) {
                                Icon(Icons.Default.Clear, contentDescription = "Clear")
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1f)
                )

                Button(
                    onClick = {
                        editingProduct = null
                        showAddProductDialog = true
                    },
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary, contentColor = Color(0xFF1C1F22)),
                    modifier = Modifier.height(52.dp).testTag("add_product_button")
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add")
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(AppStrings.addProduct(isUr))
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Category Chips Row
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(categories) { cat ->
                    val isSelected = cat == selectedCategory
                    FilterChip(
                        selected = isSelected,
                        onClick = { viewModel.selectCategory(cat) },
                        label = {
                            Text(
                                text = AppStrings.translateCategory(cat, isUr),
                                fontSize = 11.5.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor = Color(0xFF1C1F22),
                            containerColor = MaterialTheme.colorScheme.surfaceVariant,
                            labelColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Product Grid
            if (filteredProducts.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = AppStrings.noProductsFound(isUr),
                        style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                    )
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(150.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(bottom = 80.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    items(filteredProducts) { product ->
                        ProductCard(
                            product = product,
                            onAddToCart = { viewModel.addToCart(product) },
                            onEdit = {
                                editingProduct = product
                                showAddProductDialog = true
                            },
                            onDelete = { viewModel.deleteProduct(product.id) },
                            isUrdu = isUr
                        )
                    }
                }
            }
        }

        // Floating Cart Bar (Bottom)
        if (cart.isNotEmpty()) {
            Surface(
                shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp,
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Badge(containerColor = MaterialTheme.colorScheme.primary) {
                            Text(
                                text = "$cartTotalCount",
                                color = Color(0xFF1C1F22),
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Column {
                            Text(
                                text = AppStrings.currentInvoice(isUr),
                                style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                            )
                            Text(
                                text = formatCurrency(cartTotalAmount, isUr),
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            )
                        }
                    }

                    Button(
                        onClick = { showCartSheet = true },
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = Color(0xFF1C1F22)
                        ),
                        modifier = Modifier.testTag("open_cart_button")
                    ) {
                        Text(
                            text = AppStrings.reviewOrder(isUr),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }

    // Cart / Invoice Bottom Sheet
    if (showCartSheet) {
        var discountInput by remember { mutableStateOf("") }
        var discountIsPercent by remember { mutableStateOf(false) }
        var advanceInput by remember { mutableStateOf("") }
        var paymentMethod by remember { mutableStateOf("Cash") }

        val rawDiscount = discountInput.toDoubleOrNull() ?: 0.0
        val discountAmount = if (discountIsPercent) (cartTotalAmount * (rawDiscount / 100.0)).coerceIn(0.0, cartTotalAmount) else rawDiscount.coerceIn(0.0, cartTotalAmount)
        val netOrderTotal = (cartTotalAmount - discountAmount).coerceAtLeast(0.0)
        val advanceAmount = (advanceInput.toDoubleOrNull() ?: 0.0).coerceIn(0.0, netOrderTotal)
        val balanceDue = (netOrderTotal - advanceAmount).coerceAtLeast(0.0)

        ModalBottomSheet(
            onDismissRequest = { showCartSheet = false },
            containerColor = MaterialTheme.colorScheme.surface,
            scrimColor = Color.Black.copy(alpha = 0.6f)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        AppBrandLogo(modifier = Modifier.size(32.dp), showBorder = true)
                        Text(
                            text = AppStrings.currentInvoice(isUr),
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        )
                    }
                    TextButton(onClick = { viewModel.clearCart() }) {
                        Text(AppStrings.clearCart(isUr), color = PosRed)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Select Factory Dropdown
                var factoryExpanded by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(
                    expanded = factoryExpanded,
                    onExpandedChange = { factoryExpanded = !factoryExpanded },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = selectedFactory ?: AppStrings.selectFactory(isUr),
                        onValueChange = {},
                        readOnly = true,
                        label = { Text(AppStrings.factoryCustomer(isUr)) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = factoryExpanded) },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = factoryExpanded,
                        onDismissRequest = { factoryExpanded = false }
                    ) {
                        factories.forEach { factory ->
                            DropdownMenuItem(
                                text = { Text(factory.name) },
                                onClick = {
                                    viewModel.setSelectedOrderFactory(factory.name)
                                    factoryExpanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Cart Line Items List
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 180.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    cart.forEach { item ->
                        CartLineRow(
                            item = item,
                            onQuantityChange = { qty -> viewModel.updateCartQuantity(item.product.id, qty) },
                            onRemove = { viewModel.removeFromCart(item.product.id) },
                            isUrdu = isUr
                        )
                        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // --- MATHEMATICAL AUTO-SOLVE FORMULA CARD ---
                Card(
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        // Formula Header Badge
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (isUr) "خودکار حسابی فارمولا (آٹو کیلکولیشن)" else "Auto Mathematical Equation",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            )
                            Text(
                                text = "${cartTotalCount} ${if (isUr) "عدد آئٹمز" else "items"}",
                                style = MaterialTheme.typography.labelSmall.copy(color = Color.Gray)
                            )
                        }

                        // Subtotal
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(if (isUr) "کل سب ٹوٹل (قیمت × تعداد):" else "Gross Subtotal (Rate × Qty):", fontSize = 12.sp)
                            Text(formatCurrency(cartTotalAmount, isUr), fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace, fontSize = 13.sp)
                        }

                        // Discount Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = discountInput,
                                onValueChange = { discountInput = it },
                                label = { Text(if (isUr) "رعایت / ڈسکاؤنٹ" else "Discount (Rs / %)") },
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )

                            FilterChip(
                                selected = discountIsPercent,
                                onClick = { discountIsPercent = !discountIsPercent },
                                label = { Text(if (discountIsPercent) "%" else "Rs") }
                            )

                            if (discountAmount > 0) {
                                Text(
                                    "-${formatCurrency(discountAmount, isUr)}",
                                    color = PosOrange,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 12.sp
                                )
                            }
                        }

                        // Net Total (Subtotal - Discount)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                if (isUr) "خالص بل رقم (سب ٹوٹل منہا رعایت):" else "Net Total (Subtotal - Discount):",
                                fontSize = 12.5.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                formatCurrency(netOrderTotal, isUr),
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace, color = MaterialTheme.colorScheme.primary)
                            )
                        }

                        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))

                        // Advance / Paid Amount Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = advanceInput,
                                onValueChange = { advanceInput = it },
                                label = { Text(if (isUr) "پیشگی / نقد موصولہ (روپے)" else "Advance / Paid Now (Rs)") },
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )

                            // Quick Fill Chips
                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                FilterChip(
                                    selected = advanceAmount == netOrderTotal && netOrderTotal > 0,
                                    onClick = { advanceInput = netOrderTotal.toInt().toString() },
                                    label = { Text(if (isUr) "مکمل" else "100%", fontSize = 10.sp) }
                                )
                                FilterChip(
                                    selected = advanceAmount == (netOrderTotal / 2) && netOrderTotal > 0,
                                    onClick = { advanceInput = (netOrderTotal / 2).toInt().toString() },
                                    label = { Text(if (isUr) "نصف" else "50%", fontSize = 10.sp) }
                                )
                                FilterChip(
                                    selected = advanceAmount == 0.0,
                                    onClick = { advanceInput = "0" },
                                    label = { Text(if (isUr) "بغیر پیشگی" else "0", fontSize = 10.sp) }
                                )
                            }
                        }

                        // Balance Due Calculation (Net Total - Advance)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                if (isUr) "باقی واجب الادا رقم (نیٹ - ایڈوانس):" else "Balance Due (Net - Advance):",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                formatCurrency(balanceDue, isUr),
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace,
                                    color = if (balanceDue > 0) PosRed else PosGreen
                                )
                            )
                        }

                        // Method selection for advance
                        if (advanceAmount > 0) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(if (isUr) "طریقہ ادائیگی:" else "Payment via:", fontSize = 11.sp, color = Color.Gray)
                                listOf("Cash", "Online", "Bank", "Cheque").forEach { m ->
                                    val sel = m == paymentMethod
                                    FilterChip(
                                        selected = sel,
                                        onClick = { paymentMethod = m },
                                        label = { Text(m, fontSize = 10.5.sp) }
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Book Order (with auto-calculated advance and due)
                    Button(
                        onClick = {
                            viewModel.checkoutOrder(
                                paid = (advanceAmount >= netOrderTotal),
                                discount = discountAmount,
                                advancePaid = advanceAmount,
                                paymentMethod = paymentMethod
                            )
                            showCartSheet = false
                        },
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = Color(0xFF1C1F22)
                        ),
                        modifier = Modifier.weight(1f).testTag("checkout_unpaid_button")
                    ) {
                        Text(
                            text = if (advanceAmount > 0 && balanceDue > 0)
                                (if (isUr) "ایڈوانس کے ساتھ بک کریں" else "Book with Advance")
                            else if (balanceDue == 0.0)
                                (if (isUr) "مکمل نقد ادائیگی" else "Full Paid Checkout")
                            else
                                (if (isUr) "ادھار آرڈر بک کریں" else "Book Unpaid Order"),
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }

                    // Quick Full Paid Button
                    Button(
                        onClick = {
                            viewModel.checkoutOrder(
                                paid = true,
                                discount = discountAmount,
                                advancePaid = netOrderTotal,
                                paymentMethod = paymentMethod
                            )
                            showCartSheet = false
                        },
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = PosGreen,
                            contentColor = Color.White
                        ),
                        modifier = Modifier.weight(1f).testTag("checkout_paid_button")
                    ) {
                        Text(
                            text = if (isUr) "100% نقد ادا شدہ" else "100% Paid (Rs ${netOrderTotal.toInt()})",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }

    // Add / Edit Product Dialog
    if (showAddProductDialog) {
        AddEditProductDialog(
            initialProduct = editingProduct,
            selectedCategory = selectedCategory,
            onDismiss = { showAddProductDialog = false },
            onSave = { product ->
                viewModel.saveProduct(product)
                showAddProductDialog = false
            },
            isUrdu = isUr
        )
    }
}

@Composable
fun ProductCard(
    product: ProductEntity,
    onAddToCart: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
    isUrdu: Boolean = false
) {
    Card(
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        modifier = modifier
            .fillMaxWidth()
            .clickable { onAddToCart() }
            .testTag("product_card_${product.id}")
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                // Fan Guard Icon
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Cyclone,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                }

                // Edit & Delete mini buttons
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    IconButton(onClick = onEdit, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit", tint = Color.Gray, modifier = Modifier.size(13.dp))
                    }
                    IconButton(onClick = onDelete, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = PosRed, modifier = Modifier.size(13.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = product.name,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurface
                ),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            if (!product.color.isNullOrBlank() || !product.size.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = listOfNotNull(product.size, product.color, product.weight).joinToString(" · "),
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 9.5.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = formatCurrency(product.price, isUrdu),
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        color = MaterialTheme.colorScheme.primary
                    )
                )

                if (product.stock != null) {
                    val isLow = product.stock <= (product.reorderLevel ?: 5)
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = if (isLow) PosRedLight else MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Text(
                            text = if (isUrdu) "تعداد: ${product.stock}" else "Qty: ${product.stock}",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isLow) PosRed else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CartLineRow(
    item: CartItem,
    onQuantityChange: (Int) -> Unit,
    onRemove: () -> Unit,
    isUrdu: Boolean = false
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = item.product.name,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 12.5.sp
                )
            )
            Text(
                text = "${formatCurrency(item.product.price, isUrdu)} ${if (isUrdu) "فی عدد" else "each"} ${item.product.size ?: ""}",
                style = MaterialTheme.typography.bodySmall.copy(
                    fontSize = 10.5.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
        }

        // Stepper
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            IconButton(
                onClick = { onQuantityChange(item.quantity - 1) },
                modifier = Modifier
                    .size(24.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Icon(Icons.Default.Remove, contentDescription = "Decrease", modifier = Modifier.size(12.dp))
            }

            Text(
                text = "${item.quantity}",
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 13.sp
                )
            )

            IconButton(
                onClick = { onQuantityChange(item.quantity + 1) },
                modifier = Modifier
                    .size(24.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Increase", modifier = Modifier.size(12.dp))
            }

            Spacer(modifier = Modifier.width(6.dp))

            Text(
                text = formatCurrency(item.lineTotal, isUrdu),
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    color = MaterialTheme.colorScheme.primary,
                    fontSize = 12.sp
                )
            )

            IconButton(onClick = onRemove, modifier = Modifier.size(24.dp)) {
                Icon(Icons.Default.Close, contentDescription = "Remove", tint = PosRed, modifier = Modifier.size(14.dp))
            }
        }
    }
}

@Composable
fun AddEditProductDialog(
    initialProduct: ProductEntity?,
    selectedCategory: String,
    onDismiss: () -> Unit,
    onSave: (ProductEntity) -> Unit,
    isUrdu: Boolean = false
) {
    var name by remember { mutableStateOf(initialProduct?.name ?: "") }
    var priceStr by remember { mutableStateOf(initialProduct?.price?.toInt()?.toString() ?: "") }
    var size by remember { mutableStateOf(initialProduct?.size ?: "18 inch") }
    var weight by remember { mutableStateOf(initialProduct?.weight ?: "1.2 kg") }
    var color by remember { mutableStateOf(initialProduct?.color ?: "Black") }
    var stockStr by remember { mutableStateOf(initialProduct?.stock?.toString() ?: "30") }
    var reorderLevelStr by remember { mutableStateOf(initialProduct?.reorderLevel?.toString() ?: "5") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        title = {
            Text(
                text = if (initialProduct == null) {
                    if (isUrdu) "نیا پروڈکٹ شامل کریں" else "Add New Product"
                } else {
                    if (isUrdu) "پروڈکٹ تبدیل کریں" else "Edit Product"
                },
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text(if (isUrdu) "پروڈکٹ کا نام" else "Product Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = priceStr,
                        onValueChange = { priceStr = it },
                        label = { Text(if (isUrdu) "قیمت (روپے)" else "Price (Rs)") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = size,
                        onValueChange = { size = it },
                        label = { Text(if (isUrdu) "سائز" else "Size") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = color,
                        onValueChange = { color = it },
                        label = { Text(if (isUrdu) "رنگ" else "Color") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = weight,
                        onValueChange = { weight = it },
                        label = { Text(if (isUrdu) "وزن" else "Weight") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = stockStr,
                        onValueChange = { stockStr = it },
                        label = { Text(if (isUrdu) "اسٹاک تعداد" else "Stock Qty") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = reorderLevelStr,
                        onValueChange = { reorderLevelStr = it },
                        label = { Text(if (isUrdu) "کم از کم الرٹ" else "Reorder Alert") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val price = priceStr.toDoubleOrNull() ?: 0.0
                    val stock = stockStr.toIntOrNull()
                    val reorder = reorderLevelStr.toIntOrNull() ?: 5
                    if (name.isNotBlank() && price > 0) {
                        val product = initialProduct?.copy(
                            name = name,
                            price = price,
                            size = size,
                            weight = weight,
                            color = color,
                            stock = stock,
                            reorderLevel = reorder
                        ) ?: ProductEntity(
                            name = name,
                            cat = selectedCategory,
                            price = price,
                            size = size,
                            weight = weight,
                            color = color,
                            stock = stock,
                            reorderLevel = reorder
                        )
                        onSave(product)
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = Color(0xFF1C1F22)
                )
            ) {
                Text(if (isUrdu) "محفوظ کریں" else "Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(if (isUrdu) "منسوخ" else "Cancel", color = Color.Gray)
            }
        }
    )
}
